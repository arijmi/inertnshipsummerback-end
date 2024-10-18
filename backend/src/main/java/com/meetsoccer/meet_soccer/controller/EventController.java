package com.meetsoccer.meet_soccer.controller;

import com.meetsoccer.meet_soccer.dto.request.EventRequestDTO;
import com.meetsoccer.meet_soccer.dto.request.ScoreUpdateRequestDTO;
import com.meetsoccer.meet_soccer.dto.request.VoteRequestDTO;
import com.meetsoccer.meet_soccer.dto.response.EventDetailsDTO;
import com.meetsoccer.meet_soccer.dto.response.PlayerResponseDTO;
import com.meetsoccer.meet_soccer.dto.response.VotesResponseDTO;
import com.meetsoccer.meet_soccer.mapper.PlayerMapper;
import com.meetsoccer.meet_soccer.model.*;
import com.meetsoccer.meet_soccer.security.services.UserDetailsImpl;
import com.meetsoccer.meet_soccer.service.EventService;
import com.meetsoccer.meet_soccer.service.PlayerService;
import com.meetsoccer.meet_soccer.service.RefereeService;
import com.meetsoccer.meet_soccer.service.VotingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;  // Service for managing events
    private final RefereeService refereeService;  // Service for managing referees
    private final PlayerService playerService;  // Service for managing players
    private final VotingService votingService; // Service for managing votes
    private final PlayerMapper playerMapper; // Mapper for converting Player objects to DTOs

    @Operation(summary = "Retrieve all events", description = "Fetches a list of all events.")
    @ApiResponse(responseCode = "200", description = "List of events retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class)))
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.findAll();
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Retrieve all events created by the authenticated player",
            description = "Fetches a list of all events created by the currently authenticated player.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of events created by the player retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred")
    })
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getMyEvents(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Player currentPlayer = playerService.findByEmail(userDetails.getEmail());
            Set<Event> events = currentPlayer.getEvents();
            return ResponseEntity.ok(events);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Retrieve a specific event by its ID", description = "Fetches details of an event by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable @Parameter(description = "ID of the event to retrieve") Long id) {
        try {
            Event event = eventService.findById(id);
            return ResponseEntity.ok(event);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }
    }

    @Operation(summary = "Create a new event", description = "Creates a new event with the provided details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "Location is already booked for this time"),
            @ApiResponse(responseCode = "500", description = "An error occurred")
    })
    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<String> createEvent(
            @RequestBody @Valid @Parameter(description = "Details of the event to be created") EventRequestDTO eventRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (isLocationTaken(eventRequestDTO)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Location is already booked for this time");
            }

            Player currentPlayer = playerService.findByEmail(userDetails.getEmail());
            Event event = new Event(
                    eventRequestDTO.getName(),
                    eventRequestDTO.getDescription(),
                    eventRequestDTO.getEventType(),
                    eventRequestDTO.getDateTime(),
                    eventRequestDTO.getLocation(),
                    eventRequestDTO.getNumberOfPlayersPerTeam(),
                    EStatus.CREATED,
                    eventRequestDTO.getVisibility(),
                    currentPlayer
            );
            Event savedEvent = eventService.save(event);
            return ResponseEntity.status(HttpStatus.CREATED).body("Event created successfully: " + savedEvent.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Update event visibility", description = "Allows a player (creator) to set the event as private or public.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event visibility updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden, not authorized to update the event visibility"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Bad request, event cannot be updated")
    })
    @PutMapping("/{id}/visibility")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> updateEventVisibility(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @RequestParam @Parameter(description = "Visibility of the event") EEventVisibility visibility,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Event event = eventService.findById(id);
            Player currentPlayer = playerService.findByEmail(userDetails.getEmail());

            // Check if the user is the creator of the event
            if (!event.getPlayer().equals(currentPlayer)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update this event");
            }

            // Update the event visibility
            event.setVisibility(visibility);
            eventService.save(event);
            return ResponseEntity.ok("Event visibility updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Retrieve detailed information about a specific event", description = "Fetches detailed information of an event by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event details retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getEventDetails(@PathVariable @Parameter(description = "ID of the event") Long id) {
        try {
            EventDetailsDTO details = eventService.getEventDetails(id);
            return ResponseEntity.ok(details);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Join a specific event", description = "Allows a player to join an event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully joined the team"),
            @ApiResponse(responseCode = "403", description = "Event is full"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Bad request, could not join the event")
    })
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> joinEvent(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @RequestParam @Parameter(description = "Name of the team to join ('team1' or 'team2')") String teamName,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Player currentPlayer = playerService.findByEmail(userDetails.getEmail());

        try {
            if (eventService.isEventFull(eventService.findById(id))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Event is full");
            }
            eventService.joinTeam(id, currentPlayer, teamName);
            return ResponseEntity.ok("Successfully joined the team");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Leave a specific event", description = "Allows a player to leave an event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully left the team"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Bad request, could not leave the event")
    })
    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> leaveEvent(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @RequestParam @Parameter(description = "Name of the team to leave ('team1' or 'team2')") String teamName,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Player currentPlayer = playerService.findByEmail(userDetails.getEmail());

        try {
            eventService.disjoinTeam(id, currentPlayer, teamName);
            return ResponseEntity.ok("Successfully left the team");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Cancel an event", description = "Allows a player (creator) to cancel the event if it has not started yet.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event canceled successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden, not authorized to cancel the event"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Bad request, event has already started or cannot be canceled")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> cancelEvent(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Event event = eventService.findById(id);
            Player currentPlayer = playerService.findByEmail(userDetails.getEmail());

            // Check if the user is the creator of the event
            if (!event.getPlayer().equals(currentPlayer)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to cancel this event");
            }

            // Check if the event has started
            if (event.getDateTime().isBefore(ZonedDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event has already started and cannot be canceled");
            }

            // Cancel the event
            eventService.cancelEvent(id);
            return ResponseEntity.ok("Event canceled successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Assign a referee to an event", description = "Allows a referee to join an event if no referee is assigned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Referee assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Event or referee not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden, cannot be assigned as referee")
    })
    @PostMapping("/{id}/assign-referee")
    @PreAuthorize("hasRole('REFEREE')")
    public ResponseEntity<?> assignReferee(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Referee currentReferee = refereeService.findByEmail(userDetails.getEmail());
            eventService.assignReferee(id, currentReferee);
            return ResponseEntity.ok("Referee assigned successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @Operation(summary = "Leave an event as a referee", description = "Allows a referee to leave an event if the event has not started.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully left the event"),
            @ApiResponse(responseCode = "404", description = "Event or referee not found"),
            @ApiResponse(responseCode = "400", description = "Event has already started, cannot leave")
    })
    @PostMapping("/{id}/leave-referee")
    @PreAuthorize("hasRole('REFEREE')")
    public ResponseEntity<?> leaveEventAsReferee(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Referee currentReferee = refereeService.findByEmail(userDetails.getEmail());
            Event event = eventService.findById(id);

            // Check if the event has already started
            if (event.getDateTime().isBefore(ZonedDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event has already started, cannot leave");
            }

            eventService.removeReferee(event, currentReferee);
            return ResponseEntity.ok("Successfully left the event");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event or referee not found");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Remove a referee from the event", description = "Allows the player who created the event to remove the referee if the event has not started.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully removed the referee from the event"),
            @ApiResponse(responseCode = "404", description = "Event or referee not found"),
            @ApiResponse(responseCode = "403", description = "User is not the creator of the event"),
            @ApiResponse(responseCode = "400", description = "Event has already started, referee cannot be removed")
    })
    @PostMapping("/{id}/remove-referee")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> removeRefereeFromEvent(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Player currentPlayer = playerService.findByEmail(userDetails.getEmail());
            Event event = eventService.findById(id);

            // Check if the user is the player who created the event
            if (!event.getPlayer().equals(currentPlayer)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the player who created this event");
            }

            // Check if the event has already started
            if (event.getDateTime().isBefore(ZonedDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event has already started, referee cannot be removed");
            }

            eventService.removeReferee(event);
            return ResponseEntity.ok("Successfully removed the referee from the event");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event or referee not found");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Get players of each team", description = "Allows a player to view the list of players in each team for a specific event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the players in both teams"),
            @ApiResponse(responseCode = "404", description = "Event or teams not found"),
    })
    @GetMapping("/{id}/teams")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getPlayersInTeams(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Event event = eventService.findById(id);

            // Retrieve the list of players for both teams
            List<Player> teamAPlayers = new ArrayList<>(event.getTeam1());
            List<Player> teamBPlayers = new ArrayList<>(event.getTeam2());

            Map<String, List<PlayerResponseDTO>> response = new HashMap<>();
            response.put("teamA", teamAPlayers.stream().map(playerMapper::toDto).collect(Collectors.toList()));
            response.put("teamB", teamBPlayers.stream().map(playerMapper::toDto).collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event or teams not found");
        }
    }

    @Operation(summary = "Vote for a player as Man of the Match", description = "Allows a player to vote for another player in the event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vote cast successfully"),
            @ApiResponse(responseCode = "404", description = "Event or player not found"),
            @ApiResponse(responseCode = "400", description = "Player has already voted or invalid request")
    })
    @PostMapping("/{eventId}/vote")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> voteForPlayer(
            @PathVariable @Parameter(description = "ID of the event") Long eventId,
            @RequestBody @Valid VoteRequestDTO voteDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Player currentPlayer = playerService.findByEmail(userDetails.getEmail());
        try {
            if (currentPlayer.getId().equals(voteDTO.getPlayerId())) {
                return ResponseEntity.badRequest().body("Cannot vote for yourself");
            }

            votingService.voteForPlayer(voteDTO.getPlayerId(), eventId);
            return ResponseEntity.ok("Vote cast successfully");
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Get votes for an event", description = "Retrieve the vote count for each player in the event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Votes retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VotesResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{eventId}/votes")
    public ResponseEntity<?> getVotes(@PathVariable @Parameter(description = "ID of the event") Long eventId) {
        try {
            Map<Player, Integer> votes = votingService.getVotes(eventId);
            Map<Long, Integer> response = votes.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue));
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }
    }

    @Operation(summary = "Update the score of an event", description = "Allows a referee or the event creator (if there is no referee) to update the score of the event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Score updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden, not authorized to update the score"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid score format")
    })
    @PutMapping("/{id}/score")
    @PreAuthorize("hasRole('REFEREE') or hasRole('PLAYER')")
    public ResponseEntity<?> updateScore(
            @PathVariable @Parameter(description = "ID of the event") Long id,
            @RequestBody @Parameter(description = "New score of the event") ScoreUpdateRequestDTO scoreUpdateDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            // Retrieve event and user details
            Event event = eventService.findById(id);
            String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();
            if (role == "REFEREE") {
                Referee currentReferee = refereeService.findByEmail(userDetails.getEmail());
                if (event.getReferee() == null || !event.getReferee().equals(currentReferee)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update the score");
                }
            } else {
                Player currentPlayer = playerService.findByEmail(userDetails.getEmail());
                if (event.getPlayer() == null || !event.getPlayer().equals(currentPlayer)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update the score");
                }
            }
            // Update the score
            eventService.updateScore(id, scoreUpdateDTO.getScoreTeam1(), scoreUpdateDTO.getScoreTeam2());
            playerService.addGoal(scoreUpdateDTO.getGoalScorerId());
            return ResponseEntity.ok("Score updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    /**
     * Checks if the location is already booked for the specified time.
     *
     * @param eventRequestDTO the event details
     * @return true if the location is booked, false otherwise
     */
    private boolean isLocationTaken(EventRequestDTO eventRequestDTO) {
        ZonedDateTime eventStartTime = eventRequestDTO.getDateTime();
        ZonedDateTime eventEndTime = calculateEventEndTime(eventRequestDTO.getEventType(), eventStartTime);
        return eventService.isLocationTaken(eventRequestDTO.getLocation(), eventStartTime, eventEndTime);  // Check if the location is taken
    }

    /**
     * Calculates the end time of the event based on its type and start time.
     *
     * @param eventType the type of the event
     * @param startTime the start time of the event
     * @return the calculated end time
     */
    private ZonedDateTime calculateEventEndTime(Etype eventType, ZonedDateTime startTime) {
        Duration duration;
        switch (eventType) {
            case MINI_SOCCER:
                duration = Duration.ofHours(1);  // Duration for mini soccer
                break;
            case SOCCER:
                duration = Duration.ofHours(2);  // Duration for regular soccer
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);  // Throw an exception for unknown event types
        }
        return startTime.plus(duration);  // Calculate and return the end time
    }
}


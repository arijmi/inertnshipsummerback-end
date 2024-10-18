package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.dto.response.EventDetailsDTO;
import com.meetsoccer.meet_soccer.model.EStatus;
import com.meetsoccer.meet_soccer.model.Event;
import com.meetsoccer.meet_soccer.model.Player;
import com.meetsoccer.meet_soccer.model.Referee;
import com.meetsoccer.meet_soccer.repository.EventRepository;
import com.meetsoccer.meet_soccer.repository.RefereeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RefereeRepository refereeRepository;

    @Override
    public Event findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        ZonedDateTime now = ZonedDateTime.now();
        if (event.getDateTime().isBefore(now) && event.getStatus() == EStatus.CREATED) {
            event.setStatus(EStatus.STARTED);
            eventRepository.save(event);
        } else if (event.getDateTime().plusHours(2).isBefore(now) && event.getStatus() == EStatus.STARTED) {
            event.setStatus(EStatus.ENDED);
            eventRepository.save(event);
        }

        return event;
    }

    @Override
    public Set<Event> findByLocation(String location) {
        return new HashSet<>(eventRepository.findByLocation(location));
    }

    @Override
    public Set<Event> findByDateTimeBetween(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        return new HashSet<>(eventRepository.findByDateTimeBetween(startDateTime, endDateTime));
    }

    @Override
    @Transactional
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event update(Event event) {
        if (!eventRepository.existsById(event.getId())) {
            throw new EntityNotFoundException("Event not found");
        }
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found");
        }
        eventRepository.deleteById(id);
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public boolean isLocationTaken(String location, ZonedDateTime startTime, ZonedDateTime endTime) {
        return !eventRepository.findByLocationAndDateTimeBetween(location, startTime, endTime).isEmpty();
    }

    @Override
    public Set<Event> findByPlayer(Player player) {
        return new HashSet<>(eventRepository.findByPlayer(player));
    }

    @Override
    public boolean isPlayerInConflictingEvent(Player player, ZonedDateTime startTime, ZonedDateTime endTime) {
        List<Event> conflictingEvents = eventRepository.findByPlayerAndDateTimeBetween(player, startTime, endTime);
        return !conflictingEvents.isEmpty();
    }

    @Override
    public boolean isEventFull(Event event) {
        int team1Count = event.getTeam1().size();
        int team2Count = event.getTeam2().size();
        return team1Count + team2Count >= event.getNumberOfPlayersPerTeam() * 2;
    }

    @Override
    public EventDetailsDTO getEventDetails(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        Set<Player> team1 = event.getTeam1();
        Set<Player> team2 = event.getTeam2();
        int maxPlayers = event.getNumberOfPlayersPerTeam();

        return new EventDetailsDTO(
                event.getName(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                team1,
                team2,
                maxPlayers - team1.size(),
                maxPlayers - team2.size(),
                event.getVisibility()
        );
    }

    @Override
    @Transactional
    public void joinTeam(Long eventId, Player player, String teamName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (isEventFull(event)) {
            throw new IllegalStateException("Event is already full");
        }

        Set<Player> team = "team1".equalsIgnoreCase(teamName) ? event.getTeam1() : event.getTeam2();
        if (team.size() < event.getNumberOfPlayersPerTeam()) {
            team.add(player);
            eventRepository.save(event);
        } else {
            throw new IllegalStateException("Selected team is already full");
        }
    }

    @Override
    @Transactional
    public void disjoinTeam(Long eventId, Player player, String teamName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (event.getDateTime().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("Cannot leave an event that has already started");
        }

        Set<Player> team = "team1".equalsIgnoreCase(teamName) ? event.getTeam1() : event.getTeam2();
        if (team.contains(player)) {
            team.remove(player);
            eventRepository.save(event);
        } else {
            throw new IllegalStateException("Player is not in the specified team");
        }
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId, Player player) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (event.getDateTime().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("Cannot cancel an event that has already started");
        }

        if (!event.getPlayer().equals(player)) {
            throw new IllegalStateException("Only the event creator can cancel the event");
        }

        eventRepository.deleteById(eventId);
    }

    @Override
    @Transactional
    public void assignReferee(Long eventId, Referee referee) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (event.getDateTime().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("Cannot assign a referee to an event that has already started");
        }

        if (event.getReferee() != null) {
            throw new IllegalStateException("A referee is already assigned to this event");
        }

        // Assign the referee
        event.setReferee(referee);
        eventRepository.save(event);
    }

    public void removeReferee(Event event, Referee referee) {
        // Check if the event has a referee
        if (event.getReferee() == null) {
            throw new IllegalStateException("No referee assigned to this event");
        }

        // Check if the given referee is the one assigned to the event
        if (!event.getReferee().equals(referee)) {
            throw new IllegalStateException("You are not assigned as the referee for this event");
        }

        // Remove the referee from the event
        event.setReferee(null);
        eventRepository.save(event);  // Save the updated event
    }

    @Override
    public void removeReferee(Event event) {
        if (event.getReferee() == null) {
            throw new IllegalStateException("No referee assigned to this event");
        }

        event.setReferee(null);  // Remove the referee from the event
        eventRepository.save(event);  // Save the updated event
    }

    public void updateScore(Long eventId, int scoreTeam1, int scoreTeam2) {
        // Debug log to check the eventId
        System.out.println("Updating score for event with ID: " + eventRepository.findById(eventId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        event.setScoreTeam1(scoreTeam1);
        event.setScoreTeam2(scoreTeam2);
        eventRepository.save(event);
    }


    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        eventRepository.delete(event);
    }

    private ZonedDateTime calculateEventEndTime(String eventType, ZonedDateTime startTime) {
        Duration duration;
        switch (eventType.toLowerCase()) {
            case "mini soccer":
                duration = Duration.ofHours(1);
                break;
            case "soccer":
                duration = Duration.ofHours(2);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return startTime.plus(duration);
    }
}

package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.dto.response.EventDetailsDTO;
import com.meetsoccer.meet_soccer.model.Event;
import com.meetsoccer.meet_soccer.model.Player;
import com.meetsoccer.meet_soccer.model.Referee;
import jakarta.persistence.EntityNotFoundException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public interface EventService {
    Event findById(Long id);
    Set<Event> findByLocation(String location);
    Set<Event> findByDateTimeBetween(ZonedDateTime startDateTime, ZonedDateTime endDateTime);
    Event save(Event event);
    Event update(Event event);
    void deleteById(Long id);
    List<Event> findAll();
    boolean isLocationTaken(String location, ZonedDateTime startTime, ZonedDateTime endTime);
    Set<Event> findByPlayer(Player player);
    boolean isPlayerInConflictingEvent(Player player, ZonedDateTime startTime, ZonedDateTime endTime);
    boolean isEventFull(Event event);
    EventDetailsDTO getEventDetails(Long eventId);
    void joinTeam(Long eventId, Player player, String teamName);
    void disjoinTeam(Long eventId, Player player, String teamName);
    void cancelEvent(Long eventId, Player player);
    void assignReferee(Long eventId, Referee referee);
    void removeReferee(Event event, Referee referee);
    void removeReferee(Event event);
    void updateScore(Long eventId, int scoreTeam1, int scoreTeam2);
    void cancelEvent(Long eventId);

}


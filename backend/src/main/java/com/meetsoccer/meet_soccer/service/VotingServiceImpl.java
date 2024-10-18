package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Event;
import com.meetsoccer.meet_soccer.model.Player;
import com.meetsoccer.meet_soccer.repository.EventRepository;
import com.meetsoccer.meet_soccer.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VotingServiceImpl implements VotingService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public void voteForPlayer(Long playerId, Long eventId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (event.getVotes().containsKey(player)) {
            throw new IllegalStateException("Player has already voted");
        }

        event.getVotes().put(player, event.getVotes().getOrDefault(player, 0) + 1);
        eventRepository.save(event);
    }

    public Map<Player, Integer> getVotes(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        return event.getVotes();
    }
}

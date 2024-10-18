package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Player;

import java.util.Map;

public interface VotingService {
    void voteForPlayer(Long playerId, Long eventId);
    Map<Player, Integer> getVotes(Long eventId);
}

package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Player;

public interface PlayerService {
    Player findById(Long id);
    Player save(Player player);
    Player update(Player player);
    void delete(Long id);
    void addGoal(Long id);
    Player findByEmail(String email);
}


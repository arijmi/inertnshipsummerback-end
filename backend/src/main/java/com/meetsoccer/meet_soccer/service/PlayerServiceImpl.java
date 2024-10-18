package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Player;
import com.meetsoccer.meet_soccer.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    @Override
    @Transactional
    public Player save(Player player) {
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player update(Player player) {
        if (playerRepository.existsById(player.getId())) {
            return playerRepository.save(player);
        } else {
            throw new RuntimeException("Player not found");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Player not found");
        }
    }

    @Override
    public void addGoal(Long id) {
        Player player = findById(id);
        player.setGoals(player.getGoals() + 1);
        playerRepository.save(player);
    }

    @Override
    public Player findByEmail(String email) {
        return playerRepository.findByEmail(email);
    }
}


package com.meetsoccer.meet_soccer.mapper;

import com.meetsoccer.meet_soccer.dto.response.PlayerResponseDTO;
import com.meetsoccer.meet_soccer.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerResponseDTO toDto(Player player) {
        // Implement the mapping logic
        return new PlayerResponseDTO(
                player.getId(),
                player.getName(),
                player.getEmail()
        );
    }
}


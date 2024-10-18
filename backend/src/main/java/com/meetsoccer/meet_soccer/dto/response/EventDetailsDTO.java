package com.meetsoccer.meet_soccer.dto.response;

import com.meetsoccer.meet_soccer.model.EEventVisibility;
import com.meetsoccer.meet_soccer.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class EventDetailsDTO {
    private String name;
    private String description;
    private ZonedDateTime dateTime;
    private String location;
    private Set<Player> team1;
    private Set<Player> team2;
    private int emptySpotsTeam1;
    private int emptySpotsTeam2;
    private EEventVisibility visibility;
}


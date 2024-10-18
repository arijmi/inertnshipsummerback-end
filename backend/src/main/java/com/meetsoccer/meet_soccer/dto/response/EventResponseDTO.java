package com.meetsoccer.meet_soccer.dto.response;

import com.meetsoccer.meet_soccer.model.EEventVisibility;
import com.meetsoccer.meet_soccer.model.EStatus;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

@Setter
public class EventResponseDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private ZonedDateTime dateTime;
    private String location;
    private int numberOfPlayersPerTeam;
    private int scoreTeam1;
    private int scoreTeam2;
    private EStatus status;
    private Long refereeId;
    private Set<Long> team1PlayerIds;
    private Set<Long> team2PlayerIds;
    private EEventVisibility visibility;
}


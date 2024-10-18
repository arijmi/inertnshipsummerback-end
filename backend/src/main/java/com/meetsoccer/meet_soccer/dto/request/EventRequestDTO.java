package com.meetsoccer.meet_soccer.dto.request;
import com.meetsoccer.meet_soccer.model.EEventVisibility;
import com.meetsoccer.meet_soccer.model.Etype;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class EventRequestDTO {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Etype eventType;

    @NotNull
    private ZonedDateTime dateTime;

    @NotBlank
    private String location;

    @NotNull
    private int numberOfPlayersPerTeam;

    @NotNull
    private EEventVisibility visibility;
}

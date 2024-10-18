package com.meetsoccer.meet_soccer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ScoreUpdateRequestDTO {
    private int scoreTeam1;
    private int scoreTeam2;
    private Long goalScorerId;
}


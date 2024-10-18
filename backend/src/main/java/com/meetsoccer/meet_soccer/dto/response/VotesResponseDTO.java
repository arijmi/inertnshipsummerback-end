package com.meetsoccer.meet_soccer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VotesResponseDTO {

    private Map<Long, Integer> votes;

}

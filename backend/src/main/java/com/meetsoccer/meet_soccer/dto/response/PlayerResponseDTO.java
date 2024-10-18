package com.meetsoccer.meet_soccer.dto.response;

import com.meetsoccer.meet_soccer.model.EPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class PlayerResponseDTO implements Serializable {

    private Long id;
    private String name;
    private String email;
}

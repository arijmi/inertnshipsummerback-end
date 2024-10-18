package com.meetsoccer.meet_soccer.dto.response;

import lombok.Setter;

import java.io.Serializable;

@Setter
public class RefereeResponseDTO implements Serializable {

    private Long id;
    private String email;
    private String name;
    private String lastName;
    private int experience;
}

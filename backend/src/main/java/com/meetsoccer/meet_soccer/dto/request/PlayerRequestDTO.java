package com.meetsoccer.meet_soccer.dto.request;

import com.meetsoccer.meet_soccer.model.EPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class PlayerRequestDTO{

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @NotNull
    @Positive
    private int age;

    @NotNull
    private EPosition position;

    @NotNull
    @Positive
    private int goals;

    @NotNull
    @Positive
    private int gamesPlayed;

    @NotNull
    @Positive
    private int points;

    @NotNull
    @Positive
    private int level;
}
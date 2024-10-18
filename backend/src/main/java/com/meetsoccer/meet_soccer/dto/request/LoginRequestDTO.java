package com.meetsoccer.meet_soccer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDTO {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
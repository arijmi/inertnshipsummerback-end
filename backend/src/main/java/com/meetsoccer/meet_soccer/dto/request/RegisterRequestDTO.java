package com.meetsoccer.meet_soccer.dto.request;

import com.meetsoccer.meet_soccer.model.EPosition;
import com.meetsoccer.meet_soccer.model.ERole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterRequestDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private ERole role;
    private String name;
    private String lastName;
    private int age;
    private EPosition position;
    private int experience;
}

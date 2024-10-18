package com.meetsoccer.meet_soccer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.meetsoccer.meet_soccer.model.ERole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String role;

    public JwtResponse(String accessToken, Long id, String email, String role) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.role = role;
    }
    // Getters and setters
}


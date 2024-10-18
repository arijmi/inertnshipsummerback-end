package com.meetsoccer.meet_soccer.model;

import jakarta.persistence.*;
import lombok.*;

@MappedSuperclass
@Data
@NoArgsConstructor
@Getter
@Setter
public abstract class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;


    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

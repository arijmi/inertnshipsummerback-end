package com.meetsoccer.meet_soccer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "referee")
public class Referee extends User {

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int experience;

    @OneToMany(mappedBy = "referee", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    @JsonIgnore
    private Set<Event> officiatedEvents = new HashSet<>();

    public Referee(String email, String password, String name, String lastName, int experience) {
        super(email, password);
        this.name = name;
        this.lastName = lastName;
        this.experience = experience;
    }
}


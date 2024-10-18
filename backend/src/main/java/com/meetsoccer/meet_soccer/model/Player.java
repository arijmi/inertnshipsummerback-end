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
@Table(name = "player")
public class Player extends User {

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPosition position;

    @Column(nullable = false)
    private int goals = 0;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed=0;

    @Column(name = "winning_match", nullable = false)
    private int winningMatch=0;

    @Column(nullable = false)
    private int points=0;

    @Column(nullable = false)
    private double level=0;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Event> events = new HashSet<>();

    @ManyToMany(mappedBy = "team1", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<Event> eventsInTeam1 = new HashSet<>();

    @ManyToMany(mappedBy = "team2", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<Event> eventsInTeam2 = new HashSet<>();

    //constructor
    public Player(String email, String password, String name, String lastName, int age, EPosition position) {
        super(email, password);
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.position = position;
    }

    public void updatePlayerStats(boolean isWinner) {
        this.gamesPlayed++;
        if (isWinner) {
            this.winningMatch++;
        }
        this.points = goals * 10 + gamesPlayed * 5 + winningMatch * 15;
        this.level = points / 100;
    }
}



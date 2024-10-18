package com.meetsoccer.meet_soccer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private ZonedDateTime dateTime;

    @Column(nullable = false)
    private String location;

    // type of : mini soccer, soccer
    @Column(nullable = false)
    private Etype type;

    @Column(nullable = false)
    private int numberOfPlayersPerTeam;

    private int scoreTeam1;

    private int scoreTeam2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EEventVisibility visibility;

    @ElementCollection
    @MapKeyJoinColumn(name = "player_id")
    @Column(name = "vote_count")
    private Map<Player, Integer> votes = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToMany
    @JoinTable(
            name = "event_team1",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> team1 = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "event_team2",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> team2 = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "referee_id")
    private Referee referee;

    public Event(String name, String description, Etype type, ZonedDateTime dateTime, String location, int numberOfPlayersPerTeam,EStatus status, EEventVisibility visibility,Player player) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.dateTime = dateTime;
        this.location = location;
        this.numberOfPlayersPerTeam = numberOfPlayersPerTeam;
        this.status = status;
        this.player = player;
        this.visibility = visibility;
    }

    public boolean hasSpaceInTeam1() {
        return team1.size() < numberOfPlayersPerTeam;
    }

    public boolean hasSpaceInTeam2() {
        return team2.size() < numberOfPlayersPerTeam;
    }
}

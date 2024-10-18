package com.meetsoccer.meet_soccer.repository;

import com.meetsoccer.meet_soccer.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    //existsByEmail
    boolean existsByEmail(String email);

    //findByEmail
    Player findByEmail(String email);

}


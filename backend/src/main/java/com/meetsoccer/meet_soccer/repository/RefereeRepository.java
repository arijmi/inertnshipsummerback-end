package com.meetsoccer.meet_soccer.repository;

import com.meetsoccer.meet_soccer.model.Referee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Long> {
    //existsByEmail
    boolean existsByEmail(String email);

    //findByEmail
    Optional<Referee> findByEmail(String email);
}


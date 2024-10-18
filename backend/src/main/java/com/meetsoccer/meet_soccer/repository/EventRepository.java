package com.meetsoccer.meet_soccer.repository;

import com.meetsoccer.meet_soccer.model.Event;
import com.meetsoccer.meet_soccer.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Set<Event> findByLocation(String location);
    Set<Event> findByDateTimeBetween(ZonedDateTime startDateTime, ZonedDateTime endDateTime);
    @Query("SELECT e FROM Event e WHERE e.location = :location AND e.dateTime BETWEEN :startTime AND :endTime")
    List<Event> findByLocationAndDateTimeBetween(@Param("location") String location,
                                                 @Param("startTime") ZonedDateTime startTime,
                                                 @Param("endTime") ZonedDateTime endTime);
    Set<Event> findByPlayer(Player player);
    List<Event> findByPlayerAndDateTimeBetween(Player player, ZonedDateTime startTime, ZonedDateTime endTime);


}


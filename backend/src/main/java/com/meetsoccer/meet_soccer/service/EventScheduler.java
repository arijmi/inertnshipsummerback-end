package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.EStatus;
import com.meetsoccer.meet_soccer.model.Event;
import com.meetsoccer.meet_soccer.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class EventScheduler {
    @Autowired
    private EventService eventService;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void updateEventStatuses() {
        ZonedDateTime now = ZonedDateTime.now();

        List<Event> events = eventService.findAll(); // Retrieve all events
        for (Event event : events) {
            if (event.getDateTime().isBefore(now) && event.getStatus() == EStatus.CREATED) {
                event.setStatus(EStatus.STARTED);
                eventService.save(event); // Save updated status
            } else if (event.getDateTime().plusHours(2).isBefore(now) && event.getStatus() == EStatus.STARTED) {
                event.setStatus(EStatus.ENDED);
                eventService.save(event); // Save updated status
                //update all players stats
                for (Player player : event.getTeam1()) {
                    if (event.getScoreTeam1() > event.getScoreTeam2()) {
                        player.updatePlayerStats(true);
                    }
                    else {
                        player.updatePlayerStats(false);
                    }

                }
                for (Player player : event.getTeam2()) {
                    if (event.getScoreTeam1() > event.getScoreTeam2()) {
                        player.updatePlayerStats(true);
                    }
                    else {
                        player.updatePlayerStats(false);
                    }
                }
            }
        }
    }
}

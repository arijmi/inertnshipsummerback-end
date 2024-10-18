package com.meetsoccer.meet_soccer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetSoccerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetSoccerApplication.class, args);
	}

}

package com.example.playmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlaymatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaymatchApplication.class, args);
	}

}

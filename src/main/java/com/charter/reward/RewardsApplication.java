package com.charter.reward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/** Application entry point for the Rewards Program REST service. */
@SpringBootApplication
@EnableAsync
public class RewardsApplication {
    /** Starts the Spring Boot application. */
    public static void main(String[] args) {
        SpringApplication.run(RewardsApplication.class, args);
    }
}

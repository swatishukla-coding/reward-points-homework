package com.charter.reward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the retailer rewards Spring Boot application.
 */
@SpringBootApplication
public class RewardsApplication {

    /**
     * Starts the rewards application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(RewardsApplication.class, args);
    }
}

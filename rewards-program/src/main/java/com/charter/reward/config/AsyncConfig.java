package com.charter.reward.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configures asynchronous execution support for reward data lookups.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates the executor used by asynchronous reward operations.
     *
     * @return fixed-size executor service for async work
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService rewardAsyncExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}

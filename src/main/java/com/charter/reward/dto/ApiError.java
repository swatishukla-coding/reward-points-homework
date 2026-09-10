package com.charter.reward.dto;

import lombok.Getter;
import java.time.Instant;

/** Structured JSON error body returned by the REST API. */
@Getter
public class ApiError {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String message;

    /** Creates an API error with HTTP status and safe message. */
    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

package com.charter.reward.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Creates a structured error payload for API responses.
     *
     * @param status HTTP status for the error response
     * @param message descriptive message for the caller
     * @param path request path associated with the error
     */
    public ErrorResponse(HttpStatus status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    /**
     * @return timestamp when the error response was created
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return error category
     */
    public String getError() {
        return error;
    }

    /**
     * @return detailed error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return request path for the failing endpoint
     */
    public String getPath() {
        return path;
    }
}

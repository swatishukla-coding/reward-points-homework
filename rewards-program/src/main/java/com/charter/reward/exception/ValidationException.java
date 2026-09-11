package com.charter.reward.exception;

/**
 * Indicates that a request violates a service-layer input rule.
 */
public class ValidationException extends IllegalArgumentException {

    /**
     * Creates a validation failure with a caller-facing message.
     *
     * @param message validation failure description
     */
    public ValidationException(String message) {
        super(message);
    }
}
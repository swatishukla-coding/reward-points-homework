package com.charter.reward.exception;

/**
 * Raised when a requested customer cannot be found.
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing customer identifier.
     *
     * @param customerId missing customer identifier
     */
    public CustomerNotFoundException(String customerId) {
        super("No customer found with id '" + customerId + "'");
    }
}

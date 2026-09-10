package com.charter.reward.exception;

/** Raised when a syntactically valid customer id does not exist. */
public class CustomerNotFoundException extends RuntimeException {
    /** Creates the exception for the missing customer id. */
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}

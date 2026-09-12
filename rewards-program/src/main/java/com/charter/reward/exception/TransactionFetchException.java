package com.charter.reward.exception;

/**
 * Raised when transaction data cannot be retrieved for a customer.
 */
public class TransactionFetchException extends RuntimeException {

    /**
     * Creates an exception for a transaction retrieval failure.
     *
     * @param customerId customer whose transactions could not be fetched
     * @param cause underlying retrieval failure
     */
    public TransactionFetchException(String customerId, Throwable cause) {
        super("Failed to retrieve transactions for customer '" + customerId + "'", cause);
    }
}

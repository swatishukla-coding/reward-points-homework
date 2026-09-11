package com.charter.reward.service;

import com.charter.reward.model.Transaction;
import com.charter.reward.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Retrieves customer transactions using the JPA repository.
 */
@Service
public class TransactionDataService {

    private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);

    private final TransactionRepository transactionRepository;

    /**
    * Creates the transaction data service with a transaction repository dependency.
     *
    * @param transactionRepository database source for customer transactions
     */
    public TransactionDataService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves all transactions for the specified customer.
     *
     * @param customerId unique customer identifier
    * @return a completed future containing the customer's transactions
     */
    public CompletableFuture<List<Transaction>> fetchTransactionsForCustomer(String customerId) {
        log.info("Fetching transactions for customer {}", customerId);
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        log.info("Retrieved {} transaction(s) for customer {}", transactions.size(), customerId);
        return CompletableFuture.completedFuture(transactions);
    }
}

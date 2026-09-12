package com.charter.reward.service;

import com.charter.reward.model.Transaction;
import com.charter.reward.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Retrieves customer transactions using the JPA repository.
 */
@Service
public class TransactionDataService {

    private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);

    private final TransactionRepository transactionRepository;
    private final ExecutorService executorService;

    /**
     * Creates the transaction data service with a transaction repository dependency.
     *
     * @param transactionRepository database source for customer transactions
     * @param executorService executor used for asynchronous transaction lookups
     */
    public TransactionDataService(TransactionRepository transactionRepository,
                                  @Qualifier("rewardAsyncExecutor") ExecutorService executorService) {
        this.transactionRepository = transactionRepository;
        this.executorService = executorService;
    }

    /**
     * Retrieves all transactions for the specified customer.
     *
     * @param customerId unique customer identifier
     * @return the customer's transactions
     */
    public List<Transaction> fetchTransactionsForCustomer(String customerId) {
        log.info("Fetching transactions for customer {}", customerId);
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        log.info("Retrieved {} transaction(s) for customer {}", transactions.size(), customerId);
        return transactions;
    }

    /**
     * Fetches customer transactions asynchronously using the configured executor.
     *
     * @param customerId unique customer identifier
     * @return future containing the customer's transactions
     */
    public CompletableFuture<List<Transaction>> fetchTransactionsForCustomerAsync(String customerId) {
        return CompletableFuture.supplyAsync(() -> fetchTransactionsForCustomer(customerId), executorService);
    }
}

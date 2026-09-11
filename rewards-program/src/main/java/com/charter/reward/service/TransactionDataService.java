package com.charter.reward.service;

import com.charter.reward.model.Transaction;
import com.charter.reward.repository.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves customer transactions using the in-memory store.
 */
@Service
public class TransactionDataService {

    private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);

    private final TransactionStore transactionStore;

    /**
     * Creates the transaction data service with a transaction store dependency.
     *
     * @param transactionStore in-memory data source for customer transactions
     */
    public TransactionDataService(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    /**
     * Retrieves all transactions for the specified customer.
     *
     * @param customerId unique customer identifier
     * @return all transactions associated with the customer
     */
    public List<Transaction> fetchTransactionsForCustomer(String customerId) {
        log.info("Fetching transactions for customer {}", customerId);
        List<Transaction> transactions = transactionStore.findTransactionsByCustomerId(customerId);
        log.info("Retrieved {} transaction(s) for customer {}", transactions.size(), customerId);
        return transactions;
    }
}

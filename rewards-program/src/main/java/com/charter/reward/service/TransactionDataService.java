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

    public TransactionDataService(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    public List<Transaction> fetchTransactionsForCustomer(String customerId) {
        log.info("Fetching transactions for customer {}", customerId);
        List<Transaction> transactions = transactionStore.findTransactionsByCustomerId(customerId);
        log.info("Retrieved {} transaction(s) for customer {}", transactions.size(), customerId);
        return transactions;
    }
}

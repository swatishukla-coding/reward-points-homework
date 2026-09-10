package com.charter.reward.service;

import com.charter.reward.model.Transaction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Asynchronously fetches transactions belonging to a customer. */
@Service
public class TransactionFetcher {
    /** Returns only transactions for the requested customer without blocking the controller thread. */
    @Async
    public CompletableFuture<List<Transaction>> fetchByCustomerId(String customerId, List<Transaction> allTransactions) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (transaction.getCustomerId().equals(customerId)) result.add(transaction);
        }
        return CompletableFuture.completedFuture(result);
    }
}

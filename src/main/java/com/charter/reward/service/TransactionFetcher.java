package com.charter.reward.service;

import com.charter.reward.model.Transaction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionFetcher {
    @Async
    public CompletableFuture<List<Transaction>> fetchByCustomerId(String customerId, List<Transaction> allTransactions) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (transaction.getCustomerId().equals(customerId)) result.add(transaction);
        }
        return CompletableFuture.completedFuture(result);
    }
}

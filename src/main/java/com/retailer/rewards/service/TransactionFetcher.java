package com.retailer.rewards.service;

import com.retailer.rewards.model.Transaction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionFetcher {

    @Async
    public CompletableFuture<List<Transaction>> fetchByCustomerId(String customerId, List<Transaction> allTransactions) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Transaction> result = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (t.getCustomerId().equals(customerId)) {
                result.add(t);
            }
        }

        return CompletableFuture.completedFuture(result);
    }
}

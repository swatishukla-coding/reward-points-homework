package com.retailer.rewards.service;

import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class RewardService {

    private List<Customer> customers = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    @Autowired
    private TransactionFetcher transactionFetcher;

    public RewardService() {
        loadSampleData();
    }

    private void loadSampleData() {
        customers.add(new Customer("C001", "Alice Johnson"));
        customers.add(new Customer("C002", "Brian Smith"));
        customers.add(new Customer("C003", "Carla Diaz"));

        transactions.add(new Transaction("T0", "C001", LocalDate.of(2026, 5, 15), 999.00));

        transactions.add(new Transaction("T1", "C001", LocalDate.of(2026, 6, 5), 120.00));
        transactions.add(new Transaction("T2", "C001", LocalDate.of(2026, 6, 18), 45.00));
        transactions.add(new Transaction("T3", "C001", LocalDate.of(2026, 7, 2), 200.00));
        transactions.add(new Transaction("T4", "C001", LocalDate.of(2026, 7, 20), 99.99));
        transactions.add(new Transaction("T5", "C001", LocalDate.of(2026, 8, 10), 150.25));

        transactions.add(new Transaction("T6", "C002", LocalDate.of(2026, 6, 10), 250.00));
        transactions.add(new Transaction("T7", "C002", LocalDate.of(2026, 7, 5), 110.00));
        transactions.add(new Transaction("T8", "C002", LocalDate.of(2026, 8, 1), 30.00));

        transactions.add(new Transaction("T9", "C003", LocalDate.of(2026, 6, 14), 100.00));
        transactions.add(new Transaction("T10", "C003", LocalDate.of(2026, 7, 18), 130.75));
        transactions.add(new Transaction("T11", "C003", LocalDate.of(2026, 8, 22), 95.00));
    }

    public int calculatePoints(double amount) {
        int points = 0;

        if (amount > 100) {
            double amountOverHundred = amount - 100;
            points = points + (int) (amountOverHundred * 2);
            points = points + 50;
        } else if (amount > 50) {
            points = points + (int) (amount - 50);
        }

        return points;
    }

    public Map<String, Object> getRewardsForCustomer(String customerId, int months) {

        Customer customer = null;
        for (Customer c : customers) {
            if (c.getCustomerId().equals(customerId)) {
                customer = c;
            }
        }

        if (customer == null) {
            throw new CustomerNotFoundException("No customer found with id " + customerId);
        }

        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusMonths(months);

        List<Transaction> customerTransactions;
        try {
            customerTransactions = transactionFetcher.fetchByCustomerId(customerId, transactions).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not fetch transactions for " + customerId, e);
        }

        Map<String, Integer> monthlyPoints = new LinkedHashMap<>();
        int totalPoints = 0;

        for (Transaction t : customerTransactions) {

            if (t.getDate().isBefore(windowStart) || t.getDate().isAfter(today)) {
                continue;
            }

            String monthKey = t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());
            int points = calculatePoints(t.getAmount());

            if (monthlyPoints.containsKey(monthKey)) {
                monthlyPoints.put(monthKey, monthlyPoints.get(monthKey) + points);
            } else {
                monthlyPoints.put(monthKey, points);
            }

            totalPoints = totalPoints + points;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("customerId", customer.getCustomerId());
        response.put("customerName", customer.getName());
        response.put("monthlyPoints", monthlyPoints);
        response.put("totalPoints", totalPoints);

        return response;
    }
}

package com.retailer.rewards.model;

import java.time.LocalDate;

public class Transaction {

    private String transactionId;
    private String customerId;
    private LocalDate date;
    private double amount;

    public Transaction(String transactionId, String customerId, LocalDate date, double amount) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.date = date;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }
}

package com.charter.reward.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final String transactionId;
    private final String customerId;
    private final LocalDate date;
    private final BigDecimal amount;
    public Transaction(String transactionId, String customerId, LocalDate date, BigDecimal amount) {
        this.transactionId = transactionId; this.customerId = customerId; this.date = date; this.amount = amount;
    }
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public LocalDate getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
}

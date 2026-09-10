package com.charter.reward.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionReward {
    private final LocalDate date;
    private final BigDecimal amount;
    private final int points;
    public TransactionReward(LocalDate date, BigDecimal amount, int points) { this.date = date; this.amount = amount; this.points = points; }
    public LocalDate getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public int getPoints() { return points; }
}

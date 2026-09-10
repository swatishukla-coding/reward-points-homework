package com.charter.reward.dto;

import java.util.List;

/** Reward summary for one calendar month. */
public class MonthlyReward {
    private final int year;
    private final String month;
    private final int points;
    private final List<TransactionReward> transactions;

    public MonthlyReward(int year, String month, int points, List<TransactionReward> transactions) {
        this.year = year;
        this.month = month;
        this.points = points;
        this.transactions = transactions;
    }
    public int getYear() { return year; }
    public String getMonth() { return month; }
    public int getPoints() { return points; }
    public List<TransactionReward> getTransactions() { return transactions; }
}

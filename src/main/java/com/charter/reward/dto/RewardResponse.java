package com.charter.reward.dto;

import java.util.List;

/** API response containing monthly and total reward points for a customer. */
public class RewardResponse {
    private final String customerId;
    private final int months;
    private final List<MonthlyReward> monthly;
    private final int total;
    public RewardResponse(String customerId, int months, List<MonthlyReward> monthly, int total) {
        this.customerId = customerId; this.months = months; this.monthly = monthly; this.total = total;
    }
    public String getCustomerId() { return customerId; }
    public int getMonths() { return months; }
    public List<MonthlyReward> getMonthly() { return monthly; }
    public int getTotal() { return total; }
}

package com.charter.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/** Reward summary for one calendar month with its transaction breakdown. */
@Getter
@AllArgsConstructor
public class MonthlyReward {
    private final int year;
    private final String month;
    private final int points;
    private final List<TransactionReward> transactions;
}

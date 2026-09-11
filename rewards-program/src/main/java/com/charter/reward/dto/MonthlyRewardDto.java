package com.charter.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Monthly reward summary for a customer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRewardDto {

    /** e.g. "2026-07" */
    private String month;
    private int pointsEarned;
    private List<TransactionDetailDto> transactions;
}

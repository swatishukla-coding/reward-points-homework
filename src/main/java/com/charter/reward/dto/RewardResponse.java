package com.charter.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/** Typed API response containing monthly and overall reward points for a customer. */
@Getter
@AllArgsConstructor
public class RewardResponse {
    private final String customerId;
    private final int months;
    private final List<MonthlyReward> monthly;
    private final int total;
}

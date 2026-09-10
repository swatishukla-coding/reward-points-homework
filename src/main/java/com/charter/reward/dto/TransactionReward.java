package com.charter.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Reward points earned by one transaction. */
@Getter
@AllArgsConstructor
public class TransactionReward {
    private final LocalDate date;
    private final BigDecimal amount;
    private final int points;
}

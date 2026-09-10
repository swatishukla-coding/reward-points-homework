package com.charter.reward.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Purchase transaction used to calculate reward points. */
@Getter
@AllArgsConstructor
public class Transaction {
    private final String transactionId;
    private final String customerId;
    private final LocalDate date;
    private final BigDecimal amount;
}

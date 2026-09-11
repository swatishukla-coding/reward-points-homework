package com.retailer.rewards.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RewardsCalculationService {

    private static final BigDecimal LOWER_THRESHOLD = BigDecimal.valueOf(50);
    private static final BigDecimal UPPER_THRESHOLD = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        if (amount.compareTo(LOWER_THRESHOLD) <= 0) {
            return 0;
        }

        if (amount.compareTo(UPPER_THRESHOLD) <= 0) {
            return floor(amount.subtract(LOWER_THRESHOLD));
        }

        BigDecimal overHundred = amount.subtract(UPPER_THRESHOLD);
        BigDecimal points = BigDecimal.valueOf(50).add(overHundred.multiply(TWO));
        return floor(points);
    }

    private int floor(BigDecimal value) {
        return value.setScale(0, RoundingMode.DOWN).intValue();
    }
}

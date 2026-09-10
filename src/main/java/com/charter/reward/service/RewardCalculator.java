package com.charter.reward.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calculates reward points for a transaction amount. */
@Component
public class RewardCalculator {
    /**
     * Calculates points using whole-dollar thresholds: 0 through $50 = 0 points,
     * $51 through $100 = 1 point per dollar, and dollars above $100 = 2 points each.
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be zero or positive");
        }
        int dollars = amount.setScale(0, RoundingMode.DOWN).intValue();
        if (dollars > 100) return 50 + ((dollars - 100) * 2);
        if (dollars > 50) return dollars - 50;
        return 0;
    }
}

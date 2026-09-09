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

        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            BigDecimal points = amount.subtract(UPPER_THRESHOLD).multiply(TWO)
                    .add(UPPER_THRESHOLD.subtract(LOWER_THRESHOLD));
            return wholePoints(points);
        }

        if (amount.compareTo(LOWER_THRESHOLD) > 0) {
            return wholePoints(amount.subtract(LOWER_THRESHOLD));
        }

        return 0;
    }

    private int wholePoints(BigDecimal points) {
        return points.setScale(0, RoundingMode.DOWN).intValue();
    }
}

package com.charter.reward.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardCalculatorTest {
    private final RewardCalculator calculator = new RewardCalculator();

    @Test
    void calculatesThresholdsAndLargeAmounts() {
        assertEquals(0, calculator.calculatePoints(new BigDecimal("30")));
        assertEquals(0, calculator.calculatePoints(new BigDecimal("50")));
        assertEquals(25, calculator.calculatePoints(new BigDecimal("75")));
        assertEquals(50, calculator.calculatePoints(new BigDecimal("100")));
        assertEquals(90, calculator.calculatePoints(new BigDecimal("120")));
        assertEquals(850, calculator.calculatePoints(new BigDecimal("500")));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculatePoints(new BigDecimal("-1")));
    }
}

package com.retailer.rewards;

import com.retailer.rewards.service.RewardService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RewardServiceTest {

    private RewardService rewardService = new RewardService();

    @Test
    public void amountBelow50EarnsZeroPoints() {
        assertEquals(0, rewardService.calculatePoints(30));
        assertEquals(0, rewardService.calculatePoints(50));
    }

    @Test
    public void amountBetween50And100EarnsOnePointPerDollar() {
        assertEquals(25, rewardService.calculatePoints(75));
    }

    @Test
    public void amountAtHundredEarnsFiftyPoints() {
        assertEquals(50, rewardService.calculatePoints(100));
    }

    @Test
    public void exampleFromAssignment_120DollarsEarns90Points() {
        assertEquals(90, rewardService.calculatePoints(120));
    }

    @Test
    public void largeAmountCalculatesCorrectly() {
        assertEquals(850, rewardService.calculatePoints(500));
    }

    @Test
    public void unknownCustomerThrowsException() {
        try {
            rewardService.getRewardsForCustomer("DOES_NOT_EXIST", 3);
            assertEquals(true, false);
        } catch (Exception e) {
            assertEquals(true, true);
        }
    }
}

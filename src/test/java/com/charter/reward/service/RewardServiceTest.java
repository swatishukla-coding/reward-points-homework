package com.charter.reward.service;

import com.charter.reward.dto.RewardResponse;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {
    @Mock private TransactionFetcher transactionFetcher;
    private RewardService rewardService;
    private RewardCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RewardCalculator();
        rewardService = new RewardService(transactionFetcher, calculator);
    }

    @Test
    void aggregatesThreeMonthsAndGroupsTransactions() throws Exception {
        LocalDate now = LocalDate.now();
        List<Transaction> data = Arrays.asList(
                tx("A", "C001", now.withDayOfMonth(1), "120"),
                tx("B", "C001", now.minusMonths(1).withDayOfMonth(2), "75"),
                tx("C", "C001", now.minusMonths(2).withDayOfMonth(3), "100"));
        when(transactionFetcher.fetchByCustomerId(eq("C001"), anyList())).thenReturn(CompletableFuture.completedFuture(data));

        RewardResponse response = rewardService.getRewardsForCustomer("C001", 3).get();
        assertEquals(165, response.getTotal());
        assertEquals(3, response.getMonthly().size());
        assertEquals(1, response.getMonthly().get(0).getTransactions().size());
    }

    @Test
    void excludesTransactionsOutsideRequestedCalendarWindow() throws Exception {
        LocalDate now = LocalDate.now();
        List<Transaction> data = Arrays.asList(
                tx("IN", "C001", now.minusMonths(2).withDayOfMonth(1), "120"),
                tx("OUT", "C001", now.minusMonths(3).withDayOfMonth(1), "500"));
        when(transactionFetcher.fetchByCustomerId(eq("C001"), anyList())).thenReturn(CompletableFuture.completedFuture(data));
        assertEquals(90, rewardService.getRewardsForCustomer("C001", 3).get().getTotal());
    }

    @Test
    void fetchesOnlyRequestedCustomerData() throws Exception {
        when(transactionFetcher.fetchByCustomerId(eq("C002"), anyList()))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(tx("X", "C002", LocalDate.now(), "120"))));
        assertEquals(90, rewardService.getRewardsForCustomer("C002", 1).get().getTotal());
    }

    @Test
    void unknownCustomerThrowsSpecificException() {
        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> rewardService.getRewardsForCustomer("C999", 3));
        assertEquals("Customer not found: C999", ex.getMessage());
    }

    @Test
    void validatesInputParameters() {
        assertThrows(IllegalArgumentException.class, () -> rewardService.getRewardsForCustomer("C001", 0));
        assertThrows(IllegalArgumentException.class, () -> rewardService.getRewardsForCustomer("C001", -1));
        assertThrows(IllegalArgumentException.class, () -> rewardService.getRewardsForCustomer("", 3));
        assertThrows(IllegalArgumentException.class, () -> rewardService.getRewardsForCustomer("   ", 3));
    }

    private static Transaction tx(String id, String customer, LocalDate date, String amount) {
        return new Transaction(id, customer, date, new BigDecimal(amount));
    }
}

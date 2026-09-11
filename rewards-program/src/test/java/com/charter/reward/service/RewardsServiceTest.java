package com.charter.reward.service;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.dto.MonthlyRewardDto;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import com.charter.reward.repository.CustomerRepository;
import com.charter.reward.repository.TransactionRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
        private CustomerRepository customerRepository;

    @Mock
    private TransactionDataService transactionDataService;

    private RewardsService rewardsService;

    private static final LocalDate AS_OF = LocalDate.of(2026, 9, 8);

    @BeforeEach
    void setUp() {
        rewardsService = new RewardsService(customerRepository, transactionDataService, new RewardsCalculationService());
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getRewardsForCustomer("UNKNOWN", 3, AS_OF));
    }

    @Test
    void groupsPointsByMonthAndExcludesTransactionsOutsideWindow() {
        when(customerRepository.findById("C001"))
                .thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));

        List<Transaction> transactions = Arrays.asList(
                // outside the 3-month window ending 2026-09-08 (window starts 2026-06-09)
                new Transaction("T0", "C001", LocalDate.of(2026, 5, 15), new BigDecimal("999.00")),
                // June: 120 -> 90 points
                new Transaction("T1", "C001", LocalDate.of(2026, 6, 20), new BigDecimal("120.00")),
                // July: 75 -> 25 points, 45 -> 0 points
                new Transaction("T2", "C001", LocalDate.of(2026, 7, 5), new BigDecimal("75.00")),
                new Transaction("T3", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("45.00")),
                // August: 200 -> 250 points
                new Transaction("T4", "C001", LocalDate.of(2026, 8, 1), new BigDecimal("200.00"))
        );

        when(transactionDataService.fetchTransactionsForCustomer("C001"))
            .thenReturn(transactions);

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C001", 3, AS_OF);

        assertEquals("C001", response.getCustomerId());
        assertEquals(3, response.getMonthlyBreakdown().size()); // June, July, August
        assertEquals(90 + 25 + 250, response.getTotalPointsEarned());

        MonthlyRewardDto june = response.getMonthlyBreakdown().get(0);
        assertEquals("2026-06", june.getMonth());
        assertEquals(90, june.getPointsEarned());

        MonthlyRewardDto july = response.getMonthlyBreakdown().get(1);
        assertEquals("2026-07", july.getMonth());
        assertEquals(25, july.getPointsEarned());
    }

    @Test
    void customerWithNoTransactionsInWindowReturnsZeroPoints() {
        when(customerRepository.findById("C002"))
                .thenReturn(Optional.of(new Customer("C002", "Brian Smith")));
        when(transactionDataService.fetchTransactionsForCustomer("C002"))
            .thenReturn(Collections.emptyList());

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C002", 3, AS_OF);

        assertEquals(0, response.getTotalPointsEarned());
        assertEquals(0, response.getMonthlyBreakdown().size());
    }

    @Test
    void throwsForBlankCustomerId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rewardsService.getRewardsForCustomer("   ", 3, AS_OF));
        assertEquals("customerId must not be blank", ex.getMessage());
    }

    @Test
    void throwsForMissingAsOfDate() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rewardsService.getRewardsForCustomer("C001", 3, null));
        assertEquals("asOfDate must not be null", ex.getMessage());
    }

    @Test
    void allCustomersReturnsResponsesForEachCustomer() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(
                new Customer("C001", "Alice Johnson"),
                new Customer("C002", "Brian Smith")
        ));
        when(customerRepository.findById("C001")).thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));
        when(customerRepository.findById("C002")).thenReturn(Optional.of(new Customer("C002", "Brian Smith")));
        when(transactionDataService.fetchTransactionsForCustomer("C001"))
            .thenReturn(Collections.emptyList());
        when(transactionDataService.fetchTransactionsForCustomer("C002"))
            .thenReturn(Collections.emptyList());

        List<CustomerRewardsResponse> responses = rewardsService.getRewardsForAllCustomers(3, AS_OF);

        assertEquals(2, responses.size());
        assertNotNull(responses.get(0));
        assertNotNull(responses.get(1));
    }

    @Test
    void asyncTransactionLookupReturnsCompletedFuture() {
        TransactionRepository transactionRepository = org.mockito.Mockito.mock(TransactionRepository.class);
        TransactionDataService asyncDataService = new TransactionDataService(transactionRepository);

        when(transactionRepository.findByCustomerId("C001"))
            .thenReturn(Collections.singletonList(
                    new Transaction("T1", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("60.00"))));

        java.util.concurrent.CompletableFuture<List<Transaction>> future =
                asyncDataService.fetchTransactionsForCustomerAsync("C001");

        assertEquals(1, future.join().size());
    }
}

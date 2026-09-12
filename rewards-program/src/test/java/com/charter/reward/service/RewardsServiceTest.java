package com.charter.reward.service;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.dto.MonthlyRewardDto;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.exception.ValidationException;
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                new Transaction("T0", "C001", LocalDate.of(2026, 5, 15), new BigDecimal("999.00")),
                new Transaction("T1", "C001", LocalDate.of(2026, 6, 20), new BigDecimal("120.00")),
                new Transaction("T2", "C001", LocalDate.of(2026, 7, 5), new BigDecimal("75.00")),
                new Transaction("T3", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("45.00")),
                new Transaction("T4", "C001", LocalDate.of(2026, 8, 1), new BigDecimal("200.00"))
        );

        when(transactionDataService.fetchTransactionsForCustomer("C001"))
            .thenReturn(transactions);

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C001", 3, AS_OF);

        assertEquals("C001", response.getCustomerId());
    assertEquals(3, response.getMonthlyBreakdown().size());
        assertEquals(90 + 25 + 250, response.getTotalPointsEarned());

        MonthlyRewardDto june = response.getMonthlyBreakdown().get(0);
        assertEquals(2026, june.getYear());
        assertEquals(6, june.getMonthNumber());
        assertEquals("2026-06", june.getYearMonth());
        assertEquals("June", june.getMonth());
        assertEquals(90, june.getPoints());

        MonthlyRewardDto july = response.getMonthlyBreakdown().get(1);
        assertEquals(2026, july.getYear());
        assertEquals("July", july.getMonth());
        assertEquals(25, july.getPoints());

        MonthlyRewardDto august = response.getMonthlyBreakdown().get(2);
        assertEquals(2026, august.getYear());
        assertEquals("August", august.getMonth());
        assertEquals(250, august.getPoints());

        List<String> returnedTransactionIds = response.getMonthlyBreakdown().stream()
            .flatMap(monthlyReward -> monthlyReward.getTransactions().stream())
            .map(transaction -> transaction.getTransactionId())
            .collect(Collectors.toList());
        assertFalse(returnedTransactionIds.contains("T0"));
    }

        @Test
        void keepsSortableMonthKeysAcrossYearBoundary() {
        when(customerRepository.findById("C001"))
            .thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));
        when(transactionDataService.fetchTransactionsForCustomer("C001"))
            .thenReturn(Arrays.asList(
                new Transaction("T1", "C001", LocalDate.of(2025, 12, 20), new BigDecimal("120.00")),
                new Transaction("T2", "C001", LocalDate.of(2026, 1, 10), new BigDecimal("120.00"))));

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer(
            "C001", 2, LocalDate.of(2026, 1, 31));

        assertEquals("2025-12", response.getMonthlyBreakdown().get(0).getYearMonth());
        assertEquals(12, response.getMonthlyBreakdown().get(0).getMonthNumber());
        assertEquals("2026-01", response.getMonthlyBreakdown().get(1).getYearMonth());
        assertEquals(1, response.getMonthlyBreakdown().get(1).getMonthNumber());
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
    void usesRequestedLocaleForMonthNames() {
        when(customerRepository.findById("C001"))
                .thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));
        when(transactionDataService.fetchTransactionsForCustomer("C001"))
                .thenReturn(Collections.singletonList(
                        new Transaction("T1", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("120.00"))));

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer("C001", 3, AS_OF, Locale.FRENCH);

        assertEquals("juillet", response.getMonthlyBreakdown().get(0).getMonth());
    }

    @Test
    void throwsForBlankCustomerId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> rewardsService.getRewardsForCustomer("   ", 3, AS_OF));
        assertEquals("customerId must not be blank", ex.getMessage());
    }

    @Test
    void throwsForNullCustomerId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> rewardsService.getRewardsForCustomer(null, 3, AS_OF));
        assertEquals("customerId must not be blank", ex.getMessage());
    }

    @Test
    void throwsForMalformedCustomerId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> rewardsService.getRewardsForCustomer("C001!", 3, AS_OF));
        assertEquals("customerId must contain only letters, numbers, hyphen, or underscore", ex.getMessage());
    }

    @Test
    void throwsForNegativeMonths() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> rewardsService.getRewardsForCustomer("C001", -1, AS_OF));
        assertEquals("months must be greater than 0", ex.getMessage());
    }

    @Test
    void throwsForMissingAsOfDate() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> rewardsService.getRewardsForCustomer("C001", 3, null));
        assertEquals("asOfDate must not be null", ex.getMessage());
    }

    @Test
    void allCustomersReturnsResponsesForEachCustomer() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(
                new Customer("C001", "Alice Johnson"),
                new Customer("C002", "Brian Smith"),
                new Customer("C003", "Carla Diaz")
        ));
        when(customerRepository.findById("C001")).thenReturn(Optional.of(new Customer("C001", "Alice Johnson")));
        when(customerRepository.findById("C002")).thenReturn(Optional.of(new Customer("C002", "Brian Smith")));
        when(customerRepository.findById("C003")).thenReturn(Optional.of(new Customer("C003", "Carla Diaz")));
        when(transactionDataService.fetchTransactionsForCustomer("C001"))
            .thenReturn(Collections.emptyList());
        when(transactionDataService.fetchTransactionsForCustomer("C002"))
            .thenReturn(Arrays.asList(
                    new Transaction("T0009", "C002", LocalDate.of(2026, 6, 10), new BigDecimal("250.00")),
                    new Transaction("T0010", "C002", LocalDate.of(2026, 6, 22), new BigDecimal("30.00")),
                    new Transaction("T0011", "C002", LocalDate.of(2026, 7, 5), new BigDecimal("110.00")),
                    new Transaction("T0012", "C002", LocalDate.of(2026, 7, 15), new BigDecimal("85.00")),
                    new Transaction("T0013", "C002", LocalDate.of(2026, 8, 1), new BigDecimal("500.00")),
                    new Transaction("T0014", "C002", LocalDate.of(2026, 8, 19), new BigDecimal("49.99"))));
        when(transactionDataService.fetchTransactionsForCustomer("C003"))
            .thenReturn(Arrays.asList(
                    new Transaction("T0015", "C003", LocalDate.of(2026, 6, 14), new BigDecimal("100.00")),
                    new Transaction("T0016", "C003", LocalDate.of(2026, 7, 1), new BigDecimal("15.00")),
                    new Transaction("T0017", "C003", LocalDate.of(2026, 7, 18), new BigDecimal("130.75")),
                    new Transaction("T0018", "C003", LocalDate.of(2026, 8, 5), new BigDecimal("60.00")),
                    new Transaction("T0019", "C003", LocalDate.of(2026, 8, 22), new BigDecimal("95.00"))));

        List<CustomerRewardsResponse> responses = rewardsService.getRewardsForAllCustomers(3, AS_OF);

        assertEquals(3, responses.size());
        assertEquals("C001", responses.get(0).getCustomerId());
        assertEquals(0, responses.get(0).getTotalPointsEarned());
        assertEquals("C002", responses.get(1).getCustomerId());
        assertEquals(1305, responses.get(1).getTotalPointsEarned());
        assertEquals("C003", responses.get(2).getCustomerId());
        assertEquals(216, responses.get(2).getTotalPointsEarned());
    }

    @Test
    void asyncTransactionLookupUsesBackgroundExecution() throws Exception {
        TransactionRepository transactionRepository = org.mockito.Mockito.mock(TransactionRepository.class);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        TransactionDataService asyncDataService = new TransactionDataService(transactionRepository, executor);

        when(transactionRepository.findByCustomerId("C001"))
            .thenAnswer(invocation -> {
                Thread.sleep(200);
                return Collections.singletonList(
                        new Transaction("T1", "C001", LocalDate.of(2026, 7, 10), new BigDecimal("60.00")));
            });

        java.util.concurrent.CompletableFuture<List<Transaction>> future =
                asyncDataService.fetchTransactionsForCustomerAsync("C001");

        assertFalse(future.isDone());
        assertEquals(1, future.get(2, java.util.concurrent.TimeUnit.SECONDS).size());
        executor.shutdownNow();
    }
}

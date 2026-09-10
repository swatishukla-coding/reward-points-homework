package com.charter.reward.service;

import com.charter.reward.dto.MonthlyReward;
import com.charter.reward.dto.RewardResponse;
import com.charter.reward.dto.TransactionReward;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Coordinates customer lookup, transaction fetching, calendar filtering and reward aggregation. */
@Service
public class RewardService {
    private final TransactionFetcher transactionFetcher;
    private final RewardCalculator rewardCalculator;
    private final List<Customer> customers;
    private final List<Transaction> transactions;

    public RewardService(TransactionFetcher transactionFetcher, RewardCalculator rewardCalculator) {
        this(transactionFetcher, rewardCalculator, sampleCustomers(), sampleTransactions());
    }

    RewardService(TransactionFetcher transactionFetcher, RewardCalculator rewardCalculator,
                  List<Customer> customers, List<Transaction> transactions) {
        this.transactionFetcher = transactionFetcher;
        this.rewardCalculator = rewardCalculator;
        this.customers = customers;
        this.transactions = transactions;
    }

    /** Returns rewards for the requested number of full calendar months, including the current month. */
    public CompletableFuture<RewardResponse> getRewardsForCustomer(String customerId, int months) {
        validateInput(customerId, months);
        Customer customer = findCustomer(customerId);
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.minusMonths(months - 1L).atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return transactionFetcher.fetchByCustomerId(customer.getCustomerId(), transactions)
                .thenApply(items -> buildResponse(customerId, months, startDate, endDate, items));
    }

    private RewardResponse buildResponse(String customerId, int months, LocalDate startDate,
                                         LocalDate endDate, List<Transaction> items) {
        Map<YearMonth, List<TransactionReward>> grouped = new LinkedHashMap<>();
        Map<YearMonth, Integer> monthTotals = new LinkedHashMap<>();
        int total = 0;

        for (Transaction transaction : items) {
            if (transaction.getDate().isBefore(startDate) || transaction.getDate().isAfter(endDate)) continue;
            int points = rewardCalculator.calculatePoints(transaction.getAmount());
            YearMonth yearMonth = YearMonth.from(transaction.getDate());
            grouped.computeIfAbsent(yearMonth, key -> new ArrayList<>())
                    .add(new TransactionReward(transaction.getDate(), transaction.getAmount(), points));
            monthTotals.put(yearMonth, monthTotals.getOrDefault(yearMonth, 0) + points);
            total += points;
        }

        List<MonthlyReward> monthly = new ArrayList<>();
        for (Map.Entry<YearMonth, List<TransactionReward>> entry : grouped.entrySet()) {
            YearMonth ym = entry.getKey();
            monthly.add(new MonthlyReward(ym.getYear(), ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    monthTotals.get(ym), entry.getValue()));
        }
        return new RewardResponse(customerId, months, monthly, total);
    }

    private void validateInput(String customerId, int months) {
        if (customerId == null || customerId.trim().isEmpty()) throw new IllegalArgumentException("customerId must not be blank");
        if (months <= 0) throw new IllegalArgumentException("months must be greater than 0");
    }

    private Customer findCustomer(String customerId) {
        for (Customer customer : customers) if (customer.getCustomerId().equals(customerId)) return customer;
        throw new CustomerNotFoundException(customerId);
    }

    private static List<Customer> sampleCustomers() {
        return Arrays.asList(new Customer("C001", "Alice Johnson"), new Customer("C002", "Brian Smith"), new Customer("C003", "Carla Diaz"));
    }

    private static List<Transaction> sampleTransactions() {
        LocalDate now = LocalDate.now();
        return Arrays.asList(
                tx("T1", "C001", now.withDayOfMonth(1), "120.00"),
                tx("T2", "C001", now.minusMonths(1).withDayOfMonth(10), "200.00"),
                tx("T3", "C001", now.minusMonths(2).withDayOfMonth(15), "99.99"),
                tx("T4", "C001", now.minusMonths(3).withDayOfMonth(12), "150.25"),
                tx("T5", "C002", now.withDayOfMonth(2), "250.00"),
                tx("T6", "C002", now.minusMonths(1).withDayOfMonth(5), "110.00"),
                tx("T7", "C003", now.withDayOfMonth(3), "100.00"),
                tx("T8", "C003", now.minusMonths(1).withDayOfMonth(8), "130.75"));
    }

    private static Transaction tx(String id, String customer, LocalDate date, String amount) {
        return new Transaction(id, customer, date, new BigDecimal(amount));
    }
}

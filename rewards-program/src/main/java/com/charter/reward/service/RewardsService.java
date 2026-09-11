package com.charter.reward.service;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.dto.MonthlyRewardDto;
import com.charter.reward.dto.TransactionDetailDto;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.exception.TransactionFetchException;
import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import com.charter.reward.repository.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);
    private static final long FETCH_TIMEOUT_SECONDS = 5;

    private final TransactionStore transactionStore;
    private final TransactionDataService transactionDataService;
    private final RewardsCalculationService calculationService;

    public RewardsService(TransactionStore transactionStore,
                           TransactionDataService transactionDataService,
                           RewardsCalculationService calculationService) {
        this.transactionStore = transactionStore;
        this.transactionDataService = transactionDataService;
        this.calculationService = calculationService;
    }

    public CustomerRewardsResponse getRewardsForCustomer(String customerId, int months, LocalDate asOfDate) {
        Customer customer = transactionStore.findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        LocalDate periodStart = asOfDate.minusMonths(months).plusDays(1);
        Map<YearMonth, List<Transaction>> transactionsByMonth = groupTransactions(
                fetchTransactions(customerId), periodStart, asOfDate);

        List<MonthlyRewardDto> monthlyBreakdown = new ArrayList<>();
        int totalPoints = 0;

        for (YearMonth month : transactionsByMonth.keySet()) {
            MonthlyRewardDto monthlyReward = createMonthlyReward(month, transactionsByMonth.get(month));
            monthlyBreakdown.add(monthlyReward);
            totalPoints += monthlyReward.getPointsEarned();
        }

        return new CustomerRewardsResponse(
                customer.getCustomerId(), customer.getName(), periodStart, asOfDate, totalPoints, monthlyBreakdown);
    }

    public List<CustomerRewardsResponse> getRewardsForAllCustomers(int months, LocalDate asOfDate) {
        List<CustomerRewardsResponse> rewards = new ArrayList<>();
        for (Customer customer : transactionStore.findAllCustomers()) {
            rewards.add(getRewardsForCustomer(customer.getCustomerId(), months, asOfDate));
        }
        return rewards;
    }

    private Map<YearMonth, List<Transaction>> groupTransactions(List<Transaction> transactions,
                                                                  LocalDate start,
                                                                  LocalDate end) {
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getTransactionDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                filteredTransactions.add(transaction);
            }
        }

        filteredTransactions.sort(Comparator.comparing(Transaction::getTransactionDate));

        Map<YearMonth, List<Transaction>> transactionsByMonth = new TreeMap<>();
        for (Transaction transaction : filteredTransactions) {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            List<Transaction> monthTransactions = transactionsByMonth.get(month);
            if (monthTransactions == null) {
                monthTransactions = new ArrayList<>();
                transactionsByMonth.put(month, monthTransactions);
            }
            monthTransactions.add(transaction);
        }
        return transactionsByMonth;
    }

    private MonthlyRewardDto createMonthlyReward(YearMonth month, List<Transaction> transactions) {
        List<TransactionDetailDto> details = new ArrayList<>();
        int points = 0;

        for (Transaction transaction : transactions) {
            int transactionPoints = calculationService.calculatePoints(transaction.getAmount());
            points += transactionPoints;
            details.add(new TransactionDetailDto(
                    transaction.getTransactionId(),
                    transaction.getTransactionDate(),
                    transaction.getAmount(),
                    transactionPoints));
        }

        return new MonthlyRewardDto(month.toString(), points, details);
    }

    private List<Transaction> fetchTransactions(String customerId) {
        CompletableFuture<List<Transaction>> future =
            transactionDataService.fetchTransactionsForCustomer(customerId);
        try {
            return future.get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionFetchException(customerId, e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to fetch transactions for customer {}", customerId, e);
            throw new TransactionFetchException(customerId, e);
        }
    }
}

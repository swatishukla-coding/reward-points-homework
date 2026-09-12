package com.charter.reward.service;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.dto.MonthlyRewardDto;
import com.charter.reward.dto.TransactionDetailDto;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.exception.TransactionFetchException;
import com.charter.reward.exception.ValidationException;
import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import com.charter.reward.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.time.format.TextStyle;

/**
 * Business service for customer reward calculations and date-window aggregation.
 */
@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);
    private final CustomerRepository customerRepository;
    private final TransactionDataService transactionDataService;
    private final RewardsCalculationService calculationService;

    /**
      * Creates the service with the customer repository and reward calculation dependencies.
     *
      * @param customerRepository source of customer data
     * @param transactionDataService access wrapper for customer transactions
     * @param calculationService reward calculation logic used for each transaction
     */
    public RewardsService(CustomerRepository customerRepository,
                           TransactionDataService transactionDataService,
                           RewardsCalculationService calculationService) {
        this.customerRepository = customerRepository;
        this.transactionDataService = transactionDataService;
        this.calculationService = calculationService;
    }

    /**
     * Calculates the reward summary for a single customer within the requested date range.
     *
     * @param customerId unique customer identifier
     * @param months number of trailing months to include in the summary
     * @param asOfDate end date of the calculation window
     * @return reward summary for the requested customer
     */
    public CustomerRewardsResponse getRewardsForCustomer(String customerId, int months, LocalDate asOfDate) {
        return getRewardsForCustomer(customerId, months, asOfDate, Locale.getDefault());
    }

    /**
     * Calculates the reward summary for a single customer using localized month names.
     *
     * @param customerId unique customer identifier
     * @param months number of trailing months to include in the summary
     * @param asOfDate end date of the calculation window
     * @param locale locale used for month display names
     * @return reward summary for the requested customer
     */
    public CustomerRewardsResponse getRewardsForCustomer(String customerId, int months, LocalDate asOfDate,
                                                         Locale locale) {
        validateCustomerId(customerId);
        validateMonths(months);
        validateAsOfDate(asOfDate);

        log.debug("Calculating rewards for customerId={}, months={}, asOfDate={}", customerId, months, asOfDate);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        LocalDate periodStart = asOfDate.minusMonths(months).plusDays(1);
        Map<YearMonth, List<Transaction>> transactionsByMonth = groupTransactions(
                fetchTransactions(customerId), periodStart, asOfDate);

        List<MonthlyRewardDto> monthlyBreakdown = new ArrayList<>();
        int totalPoints = 0;

        for (YearMonth month : transactionsByMonth.keySet()) {
            MonthlyRewardDto monthlyReward = createMonthlyReward(month, transactionsByMonth.get(month), locale);
            monthlyBreakdown.add(monthlyReward);
            totalPoints += monthlyReward.getPoints();
        }

        log.info("Calculated rewards for customerId={} from {} to {}: totalPoints={}, monthCount={}",
            customerId, periodStart, asOfDate, totalPoints, monthlyBreakdown.size());
        return new CustomerRewardsResponse(
                customer.getCustomerId(), customer.getName(), periodStart, asOfDate, totalPoints, monthlyBreakdown);
    }

    /**
     * Calculates reward summaries for every customer in the configured date range.
     *
     * @param months number of trailing months to include in each summary
     * @param asOfDate end date of the calculation window
     * @return reward summaries for each stored customer
     */
    public List<CustomerRewardsResponse> getRewardsForAllCustomers(int months, LocalDate asOfDate) {
        return getRewardsForAllCustomers(months, asOfDate, Locale.getDefault());
    }

    /**
     * Calculates reward summaries for every customer using localized month names.
     *
     * @param months number of trailing months to include in each summary
     * @param asOfDate end date of the calculation window
     * @param locale locale used for month display names
     * @return reward summaries for each stored customer
     */
    public List<CustomerRewardsResponse> getRewardsForAllCustomers(int months, LocalDate asOfDate, Locale locale) {
        validateMonths(months);
        validateAsOfDate(asOfDate);

        log.debug("Calculating rewards for all customers, months={}, asOfDate={}", months, asOfDate);
        List<CustomerRewardsResponse> rewards = new ArrayList<>();
        for (Customer customer : customerRepository.findAll()) {
            rewards.add(getRewardsForCustomer(customer.getCustomerId(), months, asOfDate, locale));
        }
        log.info("Calculated rewards for {} customer(s)", rewards.size());
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

    private MonthlyRewardDto createMonthlyReward(YearMonth month, List<Transaction> transactions, Locale locale) {
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

        return new MonthlyRewardDto(
            month.getYear(),
            month.getMonthValue(),
            month.getMonth().getDisplayName(TextStyle.FULL, locale),
            points,
            details);
    }

    private List<Transaction> fetchTransactions(String customerId) {
        try {
            return transactionDataService.fetchTransactionsForCustomer(customerId);
        } catch (RuntimeException e) {
            log.error("Failed to fetch transactions for customer {}", customerId, e);
            throw new TransactionFetchException(customerId, e);
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            log.warn("Invalid rewards request: blank customerId");
            throw new ValidationException("customerId must not be blank");
        }
        if (!customerId.matches("^[A-Za-z0-9_-]+$")) {
            log.warn("Invalid rewards request: malformed customerId={}", customerId);
            throw new ValidationException("customerId must contain only letters, numbers, hyphen, or underscore");
        }
    }

    private void validateMonths(int months) {
        if (months <= 0) {
            log.warn("Invalid rewards request: months={}", months);
            throw new ValidationException("months must be greater than 0");
        }
    }

    private void validateAsOfDate(LocalDate asOfDate) {
        if (asOfDate == null) {
            log.warn("Invalid rewards request: asOfDate is null");
            throw new ValidationException("asOfDate must not be null");
        }
    }
}

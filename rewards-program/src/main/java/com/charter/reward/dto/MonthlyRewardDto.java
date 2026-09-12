package com.charter.reward.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Monthly reward summary for a customer.
 */
@Getter
@Setter
@NoArgsConstructor
public class MonthlyRewardDto {

    private int year;
    private int monthNumber;
    private String yearMonth;
    private String month;
    private int points;
    private List<TransactionDetailDto> transactions;

    /**
     * Creates a monthly reward summary with both display and sortable month values.
     *
     * @param year calendar year
     * @param monthNumber calendar month number from 1 to 12
     * @param month localized month display name
     * @param points reward points earned in the month
     * @param transactions transactions included in the month
     */
    public MonthlyRewardDto(int year, int monthNumber, String month, int points,
                            List<TransactionDetailDto> transactions) {
        this.year = year;
        this.monthNumber = monthNumber;
        this.yearMonth = String.format("%04d-%02d", year, monthNumber);
        this.month = month;
        this.points = points;
        this.transactions = transactions;
    }
}

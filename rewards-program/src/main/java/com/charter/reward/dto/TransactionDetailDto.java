package com.charter.reward.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Per-transaction reward detail.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDto {

    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal amount;
    private int pointsEarned;
}

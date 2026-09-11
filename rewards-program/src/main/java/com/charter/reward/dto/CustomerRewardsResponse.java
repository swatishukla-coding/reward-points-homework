package com.charter.reward.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Response payload for customer reward summary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardsResponse {

    private String customerId;
    private String customerName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;

    private int totalPointsEarned;
    private List<MonthlyRewardDto> monthlyBreakdown;
}

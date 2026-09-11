package com.charter.reward.controller;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.dto.MonthlyRewardDto;
import com.charter.reward.dto.TransactionDetailDto;
import com.charter.reward.exception.ValidationException;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    @Test
    void returnsRewardsForKnownCustomer() throws Exception {
        CustomerRewardsResponse response = new CustomerRewardsResponse(
                "C001", "Alice Johnson",
                LocalDate.of(2026, 6, 9), LocalDate.of(2026, 9, 8),
                90, Collections.singletonList(new MonthlyRewardDto("2026-06", 90, Collections.singletonList(
                        new TransactionDetailDto("T0001", LocalDate.of(2026, 6, 20),
                                new BigDecimal("120.00"), 90)))));

        when(rewardsService.getRewardsForCustomer(eq("C001"), anyInt(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalPointsEarned").value(90))
                .andExpect(jsonPath("$.monthlyBreakdown[0].month").value("2026-06"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionId").value("T0001"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionDate").value("2026-06-20"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].amount").value(120.00))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].pointsEarned").value(90));
    }

    @Test
    void returns404ForUnknownCustomer() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("UNKNOWN"), anyInt(), any()))
                .thenThrow(new CustomerNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/rewards/customers/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns400ForInvalidMonthsParameter() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("C001"), eq(0), any()))
                .thenThrow(new ValidationException("months must be greater than 0"));

        mockMvc.perform(get("/api/v1/rewards/customers/C001").param("months", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WithNormalizedMessageForInvalidDate() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/C001")
                        .param("asOfDate", "2026/09/08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'asOfDate'"));
    }

    @Test
    void returns405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers/C001"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message")
                        .value("HTTP method is not supported for this endpoint"));
    }
}

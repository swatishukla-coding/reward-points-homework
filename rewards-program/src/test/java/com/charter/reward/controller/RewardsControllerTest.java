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
import java.util.Locale;
import java.util.concurrent.CompletionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
                90, Collections.singletonList(new MonthlyRewardDto(2026, 6, "June", 90, Collections.singletonList(
                        new TransactionDetailDto("T0001", LocalDate.of(2026, 6, 20),
                                new BigDecimal("120.00"), 90)))));

        when(rewardsService.getRewardsForCustomer(eq("C001"), anyInt(), any(), any(Locale.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalPointsEarned").value(90))
                .andExpect(jsonPath("$.monthlyBreakdown[0].year").value(2026))
                .andExpect(jsonPath("$.monthlyBreakdown[0].monthNumber").value(6))
                .andExpect(jsonPath("$.monthlyBreakdown[0].yearMonth").value("2026-06"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].month").value("June"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].points").value(90))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionId").value("T0001"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionDate").value("2026-06-20"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].amount").value(120.00))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].pointsEarned").value(90));
    }

    @Test
    void missingOptionalQueryParamsUseDefaults() throws Exception {
        CustomerRewardsResponse response = new CustomerRewardsResponse(
                "C001", "Alice Johnson",
                LocalDate.now().minusMonths(3).plusDays(1), LocalDate.now(),
                0, Collections.emptyList());

        when(rewardsService.getRewardsForCustomer(eq("C001"), eq(3), any(), any(Locale.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"));

        verify(rewardsService).getRewardsForCustomer(eq("C001"), eq(3), any(), any(Locale.class));
    }

    @Test
    void returns404ForUnknownCustomer() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("UNKNOWN"), anyInt(), any(), any(Locale.class)))
                .thenThrow(new CustomerNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/rewards/customers/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/rewards/customers/UNKNOWN"))
                .andExpect(jsonPath("$.message").value("No customer found with id 'UNKNOWN'"));
    }

    @Test
    void returns404ForAsyncWrappedUnknownCustomer() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("UNKNOWN"), anyInt(), any(), any(Locale.class)))
                .thenThrow(new CompletionException(new CustomerNotFoundException("UNKNOWN")));

        mockMvc.perform(get("/api/v1/rewards/customers/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No customer found with id 'UNKNOWN'"));
    }

    @Test
    void returns400ForInvalidMonthsParameter() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("C001"), eq(0), any(), any(Locale.class)))
                .thenThrow(new ValidationException("months must be greater than 0"));

        mockMvc.perform(get("/api/v1/rewards/customers/C001").param("months", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/rewards/customers/C001"))
                .andExpect(jsonPath("$.message").value("months must be greater than 0"));
    }

    @Test
    void returns400ForMalformedCustomerId() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("C001!"), anyInt(), any(), any(Locale.class)))
                .thenThrow(new ValidationException(
                        "customerId must contain only letters, numbers, hyphen, or underscore"));

        mockMvc.perform(get("/api/v1/rewards/customers/C001!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("customerId must contain only letters, numbers, hyphen, or underscore"));
    }

    @Test
    void returns400ForBlankCustomerId() throws Exception {
        when(rewardsService.getRewardsForCustomer(anyString(), anyInt(), any(), any(Locale.class)))
                .thenThrow(new ValidationException("customerId must not be blank"));

        mockMvc.perform(get("/api/v1/rewards/customers/%20%20%20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("customerId must not be blank"));
    }

    @Test
    void returns400WithNormalizedMessageForEmptyValidationException() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("C001"), anyInt(), any(), any(Locale.class)))
                .thenThrow(new ValidationException("   "));

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void returns500ForUnrelatedIllegalArgumentException() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq("C001"), anyInt(), any(), any(Locale.class)))
                .thenThrow(new IllegalArgumentException("Unexpected internal argument error"));

        mockMvc.perform(get("/api/v1/rewards/customers/C001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void returns400WithNormalizedMessageForInvalidDate() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/C001")
                        .param("asOfDate", "2026/09/08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'asOfDate'"));
    }

        @Test
        void returns404ForUnknownRoute() throws Exception {
                mockMvc.perform(get("/api/v1/rewards/missing-route"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("The requested endpoint was not found"));
        }

    @Test
    void returns405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers/C001"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message")
                        .value("HTTP method is not supported for this endpoint"));
    }
}

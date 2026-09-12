package com.charter.reward.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack MockMvc tests for the rewards REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsRewardsForSeededCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/C001")
                        .param("months", "3")
                        .param("asOfDate", "2026-09-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalPointsEarned").value(484))
                .andExpect(jsonPath("$.monthlyBreakdown[0].year").value(2026))
                .andExpect(jsonPath("$.monthlyBreakdown[0].monthNumber").value(6))
                .andExpect(jsonPath("$.monthlyBreakdown[0].yearMonth").value("2026-06"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].month").value("June"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].points").value(25))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions.length()").value(2))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionId").value("T0003"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].transactionDate").value("2026-06-18"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].amount").value(45.00))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[0].pointsEarned").value(0))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[1].transactionId").value("T0004"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[1].transactionDate").value("2026-06-25"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[1].amount").value(75.50))
                .andExpect(jsonPath("$.monthlyBreakdown[0].transactions[1].pointsEarned").value(25))
                .andExpect(jsonPath("$.monthlyBreakdown[1].month").value("July"))
                .andExpect(jsonPath("$.monthlyBreakdown[1].points").value(299))
                .andExpect(jsonPath("$.monthlyBreakdown[1].transactions.length()").value(2))
                .andExpect(jsonPath("$.monthlyBreakdown[1].transactions[0].transactionId").value("T0005"))
                .andExpect(jsonPath("$.monthlyBreakdown[1].transactions[0].pointsEarned").value(250))
                .andExpect(jsonPath("$.monthlyBreakdown[2].month").value("August"))
                .andExpect(jsonPath("$.monthlyBreakdown[2].points").value(160))
                .andExpect(jsonPath("$.monthlyBreakdown[2].transactions.length()").value(2))
                .andExpect(jsonPath("$.monthlyBreakdown[2].transactions[0].transactionId").value("T0007"))
                .andExpect(jsonPath("$.monthlyBreakdown[2].transactions[0].pointsEarned").value(150));
    }

    @Test
    void returns404ForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/UNKNOWN")
                        .param("months", "3")
                        .param("asOfDate", "2026-09-08"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No customer found with id 'UNKNOWN'"));
    }

    @Test
    void returns400ForInvalidMonths() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/C001")
                        .param("months", "0")
                        .param("asOfDate", "2026-09-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("months must be greater than 0"));
    }

    @Test
    void returns404ForUnknownRoute() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/missing-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/v1/rewards/missing-route"))
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

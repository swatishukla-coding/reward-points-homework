package com.charter.reward.controller;

import com.charter.reward.dto.RewardResponse;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
class RewardControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private RewardService rewardService;

    @Test
    void successReturns200() throws Exception {
        RewardResponse response = new RewardResponse("C001", 3, Collections.emptyList(), 165);
        when(rewardService.getRewardsForCustomer("C001", 3))
                .thenReturn(CompletableFuture.completedFuture(response));

        org.springframework.test.web.servlet.MvcResult result = mockMvc.perform(get("/api/rewards/C001").param("months", "3"))
                .andExpect(request().asyncStarted()).andReturn();
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.total").value(165));
    }

    @Test
    void unknownCustomerReturns404() throws Exception {
        when(rewardService.getRewardsForCustomer("C999", 3)).thenThrow(new CustomerNotFoundException("C999"));
        mockMvc.perform(get("/api/rewards/C999").param("months", "3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void missingMonthsReturns400() throws Exception {
        mockMvc.perform(get("/api/rewards/C001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void zeroMonthsReturns400() throws Exception {
        mockMvc.perform(get("/api/rewards/C001").param("months", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedCustomerIdReturns400() throws Exception {
        mockMvc.perform(get("/api/rewards/ABC").param("months", "3"))
                .andExpect(status().isBadRequest());
    }
}

package com.charter.reward.controller;

import com.charter.reward.dto.RewardResponse;
import com.charter.reward.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.concurrent.CompletableFuture;

/** REST API for retrieving reward points for a customer. */
@RestController
@RequestMapping("/api/rewards")
@Validated
public class RewardController {
    private static final Logger log = LoggerFactory.getLogger(RewardController.class);
    private final RewardService rewardService;

    /** Creates the controller with its service dependency. */
    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /** Returns transaction, monthly and overall reward totals for the requested calendar window. */
    @GetMapping("/{customerId}")
    public CompletableFuture<ResponseEntity<RewardResponse>> getRewards(
            @PathVariable @Pattern(regexp = "C\\d{3}", message = "customerId must match C followed by 3 digits") String customerId,
            @RequestParam @Min(value = 1, message = "months must be greater than 0") Integer months) {
        log.info("Reward request received for customerId={} months={}", customerId, months);
        return rewardService.getRewardsForCustomer(customerId, months).thenApply(response -> {
            log.info("Reward request completed for customerId={} totalPoints={}", customerId, response.getTotal());
            return ResponseEntity.ok(response);
        });
    }
}

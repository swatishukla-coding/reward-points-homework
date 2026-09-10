package com.charter.reward.controller;

import com.charter.reward.dto.RewardResponse;
import com.charter.reward.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    private final RewardService rewardService;
    public RewardController(RewardService rewardService) { this.rewardService = rewardService; }

    @GetMapping("/{customerId}")
    public CompletableFuture<ResponseEntity<RewardResponse>> getRewards(
            @PathVariable String customerId,
            @RequestParam int months) {
        return rewardService.getRewardsForCustomer(customerId, months).thenApply(ResponseEntity::ok);
    }
}

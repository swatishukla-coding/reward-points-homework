package com.retailer.rewards.controller;

import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getRewards(@PathVariable String customerId,
                                         @RequestParam(defaultValue = "3") int months) {

        if (months <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("months must be greater than 0");
        }

        try {
            Map<String, Object> rewards = rewardService.getRewardsForCustomer(customerId, months);
            return ResponseEntity.ok(rewards);
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

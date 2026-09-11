package com.charter.reward.controller;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.exception.CustomerNotFoundException;
import com.charter.reward.service.RewardsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardsResponse> getRewardsForCustomer(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "3") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        if (months <= 0) {
            throw new IllegalArgumentException("months must be greater than 0");
        }

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer(customerId, months,
                asOfDate != null ? asOfDate : LocalDate.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerRewardsResponse>> getRewardsForAllCustomers(
            @RequestParam(defaultValue = "3") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        if (months <= 0) {
            throw new IllegalArgumentException("months must be greater than 0");
        }

        return ResponseEntity.ok(rewardsService.getRewardsForAllCustomers(months,
                asOfDate != null ? asOfDate : LocalDate.now()));
    }
}
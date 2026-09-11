package com.charter.reward.controller;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.service.RewardsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * REST endpoints for customer reward lookups.
 */
@RestController
@RequestMapping("/api/v1/rewards")
@Validated
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardsResponse> getRewardsForCustomer(
            @PathVariable @NotBlank String customerId,
            @RequestParam(defaultValue = "3") @Min(1) int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer(customerId, months,
                asOfDate != null ? asOfDate : LocalDate.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerRewardsResponse>> getRewardsForAllCustomers(
            @RequestParam(defaultValue = "3") @Min(1) int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return ResponseEntity.ok(rewardsService.getRewardsForAllCustomers(months,
                asOfDate != null ? asOfDate : LocalDate.now()));
    }
}
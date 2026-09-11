package com.charter.reward.controller;

import com.charter.reward.dto.CustomerRewardsResponse;
import com.charter.reward.service.RewardsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST endpoints for customer reward lookups.
 *
 * <p>Validation is enforced in the service layer so the HTTP layer remains
 * focused on request mapping and the business rules stay consistent across all
 * callers.</p>
 */
@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Creates the controller with the rewards service dependency.
     *
     * @param rewardsService service used to calculate and aggregate rewards
     */
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Retrieves the reward summary for a single customer.
     *
     * @param customerId customer identifier to look up
     * @param months number of trailing months to include in the summary
     * @param asOfDate optional end date for the calculation window; defaults to today
     * @return reward summary for the requested customer
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardsResponse> getRewardsForCustomer(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "3") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        CustomerRewardsResponse response = rewardsService.getRewardsForCustomer(customerId, months,
                asOfDate != null ? asOfDate : LocalDate.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves reward summaries for all customers.
     *
     * @param months number of trailing months to include in each summary
     * @param asOfDate optional end date for the reward calculation window; defaults to today
     * @return list of reward summaries for each customer
     */
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerRewardsResponse>> getRewardsForAllCustomers(
            @RequestParam(defaultValue = "3") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return ResponseEntity.ok(rewardsService.getRewardsForAllCustomers(months,
                asOfDate != null ? asOfDate : LocalDate.now()));
    }
}
package com.charter.reward.repository;

import com.charter.reward.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for purchase transaction records.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Finds all transactions recorded for a customer.
     *
     * @param customerId customer identifier to search by
     * @return transactions belonging to the customer
     */
    List<Transaction> findByCustomerId(String customerId);
}

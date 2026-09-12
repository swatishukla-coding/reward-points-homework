package com.charter.reward.repository;

import com.charter.reward.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for customer records.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
}

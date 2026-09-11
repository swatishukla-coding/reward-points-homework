package com.charter.reward.config;

import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;
import com.charter.reward.repository.CustomerRepository;
import com.charter.reward.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CustomerRepository customerRepository,
                               TransactionRepository transactionRepository) {
        return args -> {
            if (customerRepository.count() == 0) {
                customerRepository.saveAll(Arrays.asList(
                        new Customer("C001", "Alice Johnson"),
                        new Customer("C002", "Brian Smith"),
                        new Customer("C003", "Carla Diaz")
                ));
            }

            if (transactionRepository.count() == 0) {
                transactionRepository.saveAll(Arrays.asList(
                        new Transaction("T0001", "C001", LocalDate.of(2026, 5, 15), new BigDecimal("999.00")),
                        new Transaction("T0002", "C001", LocalDate.of(2026, 6, 5), new BigDecimal("120.00")),
                        new Transaction("T0003", "C001", LocalDate.of(2026, 6, 18), new BigDecimal("45.00")),
                        new Transaction("T0004", "C001", LocalDate.of(2026, 6, 25), new BigDecimal("75.50")),
                        new Transaction("T0005", "C001", LocalDate.of(2026, 7, 2), new BigDecimal("200.00")),
                        new Transaction("T0006", "C001", LocalDate.of(2026, 7, 20), new BigDecimal("99.99")),
                        new Transaction("T0007", "C001", LocalDate.of(2026, 8, 10), new BigDecimal("150.25")),
                        new Transaction("T0008", "C001", LocalDate.of(2026, 8, 28), new BigDecimal("60.00")),

                        new Transaction("T0009", "C002", LocalDate.of(2026, 6, 10), new BigDecimal("250.00")),
                        new Transaction("T0010", "C002", LocalDate.of(2026, 6, 22), new BigDecimal("30.00")),
                        new Transaction("T0011", "C002", LocalDate.of(2026, 7, 5), new BigDecimal("110.00")),
                        new Transaction("T0012", "C002", LocalDate.of(2026, 7, 15), new BigDecimal("85.00")),
                        new Transaction("T0013", "C002", LocalDate.of(2026, 8, 1), new BigDecimal("500.00")),
                        new Transaction("T0014", "C002", LocalDate.of(2026, 8, 19), new BigDecimal("49.99")),

                        new Transaction("T0015", "C003", LocalDate.of(2026, 6, 14), new BigDecimal("100.00")),
                        new Transaction("T0016", "C003", LocalDate.of(2026, 7, 1), new BigDecimal("15.00")),
                        new Transaction("T0017", "C003", LocalDate.of(2026, 7, 18), new BigDecimal("130.75")),
                        new Transaction("T0018", "C003", LocalDate.of(2026, 8, 5), new BigDecimal("60.00")),
                        new Transaction("T0019", "C003", LocalDate.of(2026, 8, 22), new BigDecimal("95.00"))
                ));
            }
        };
    }
}

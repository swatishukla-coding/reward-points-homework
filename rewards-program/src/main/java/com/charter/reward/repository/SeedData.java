package com.charter.reward.repository;

import com.charter.reward.model.Customer;
import com.charter.reward.model.Transaction;

import java.util.List;

/**
 * Maps directly onto data/seed-data.json. Only used at startup to seed the
 * in-memory store, so it stays package-private and dumb.
 */
class SeedData {

    private List<Customer> customers;
    private List<Transaction> transactions;

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}

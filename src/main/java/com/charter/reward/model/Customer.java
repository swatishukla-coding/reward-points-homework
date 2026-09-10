package com.charter.reward.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** In-memory customer used by the rewards example. */
@Getter
@AllArgsConstructor
public class Customer {
    private final String customerId;
    private final String name;
}

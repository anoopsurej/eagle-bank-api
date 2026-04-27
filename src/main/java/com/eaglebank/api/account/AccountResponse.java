package com.eaglebank.api.account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        String accountNumber,
        String sortCode,
        String name,
        AccountType accountType,
        BigDecimal balance,
        String currency,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
}
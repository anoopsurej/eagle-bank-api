package com.eaglebank.api.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        BigDecimal amount,
        String currency,
        TransactionType type,
        String reference,
        String userId,
        Instant createdTimestamp
) {
}
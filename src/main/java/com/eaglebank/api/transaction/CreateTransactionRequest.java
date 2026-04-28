package com.eaglebank.api.transaction;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull @Positive @DecimalMax("10000.00") BigDecimal amount,
        @NotNull TransactionType type,
        String reference
) {
}
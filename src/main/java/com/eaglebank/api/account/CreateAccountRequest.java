package com.eaglebank.api.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
    @NotBlank String name,
    @NotNull AccountType accountType
) {
}
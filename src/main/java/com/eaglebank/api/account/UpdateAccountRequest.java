package com.eaglebank.api.account;

public record UpdateAccountRequest(
    String name,
    AccountType accountType
) {
}

package com.eaglebank.api.account;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class AccountMapper {

    private static final String SORT_CODE = "10-10-10";
    private static final String CURRENCY = "GBP";

    public Account toAccount(CreateAccountRequest request, String accountNumber, String userId) {
        Instant now = Instant.now();
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUserId(userId);
        account.setName(request.name());
        account.setAccountType(request.accountType());
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedTimestamp(now);
        account.setUpdatedTimestamp(now);
        return account;
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                SORT_CODE,
                account.getName(),
                account.getAccountType(),
                account.getBalance(),
                CURRENCY,
                account.getCreatedTimestamp(),
                account.getUpdatedTimestamp()
        );
    }

    public void updateAccount(Account account, UpdateAccountRequest request) {
        if (request.name() != null) account.setName(request.name());
        if (request.accountType() != null) account.setAccountType(request.accountType());
        account.setUpdatedTimestamp(Instant.now());
    }
}

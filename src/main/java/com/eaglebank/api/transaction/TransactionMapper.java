package com.eaglebank.api.transaction;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionMapper {

    private static final String CURRENCY = "GBP";

    public Transaction toTransaction(CreateTransactionRequest request, String id,
                                      String accountNumber, String userId) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAccountNumber(accountNumber);
        transaction.setUserId(userId);
        transaction.setAmount(request.amount());
        transaction.setCurrency(CURRENCY);
        transaction.setType(request.type());
        transaction.setReference(request.reference());
        transaction.setCreatedTimestamp(Instant.now());
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getReference(),
                transaction.getUserId(),
                transaction.getCreatedTimestamp()
        );
    }
}

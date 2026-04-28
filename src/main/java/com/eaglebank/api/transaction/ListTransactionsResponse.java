package com.eaglebank.api.transaction;

import java.util.List;

public record ListTransactionsResponse(List<TransactionResponse> transactions) {
}

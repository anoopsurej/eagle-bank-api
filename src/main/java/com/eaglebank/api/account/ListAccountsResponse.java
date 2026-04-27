package com.eaglebank.api.account;

import java.util.List;

public record ListAccountsResponse(List<AccountResponse> accounts) {
}

package com.eaglebank.api.account;

import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PrefixIdGenerator idGenerator;

    public AccountResponse createAccount(CreateAccountRequest request, String userId) {
        Account account = accountMapper.toAccount(request, idGenerator.accountNumber(), userId);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    public ListAccountsResponse listAccounts(String userId) {
        List<AccountResponse> accounts = accountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
        return new ListAccountsResponse(accounts);
    }

    public AccountResponse fetchAccount(String accountNumber, String userId) {
        Account account = findAndAuthorize(accountNumber, userId);
        return accountMapper.toResponse(account);
    }

    public AccountResponse updateAccount(String accountNumber, UpdateAccountRequest request, String userId) {
        Account account = findAndAuthorize(accountNumber, userId);
        accountMapper.updateAccount(account, request);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    public void deleteAccount(String accountNumber, String userId) {
        Account account = findAndAuthorize(accountNumber, userId);
        accountRepository.delete(account);
    }

    private Account findAndAuthorize(String accountNumber, String userId) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }
        return account;
    }
}

package com.eaglebank.api.account;

import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PrefixIdGenerator idGenerator;

    public AccountResponse createAccount(CreateAccountRequest request, String userId) {
        log.info("Creating account userId={} accountType={}", userId, request.accountType());
        Account account = accountMapper.toAccount(request, idGenerator.accountNumber(), userId);
        AccountResponse response = accountMapper.toResponse(accountRepository.save(account));
        log.info("Account created accountNumber={}", response.accountNumber());
        return response;
    }

    public ListAccountsResponse listAccounts(String userId) {
        log.debug("Listing accounts userId={}", userId);
        List<AccountResponse> accounts = accountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
        return new ListAccountsResponse(accounts);
    }

    public AccountResponse fetchAccount(String accountNumber, String userId) {
        log.debug("Fetching account accountNumber={}", accountNumber);
        return accountMapper.toResponse(findAndAuthorize(accountNumber, userId));
    }

    public AccountResponse updateAccount(String accountNumber, UpdateAccountRequest request, String userId) {
        log.info("Updating account accountNumber={}", accountNumber);
        Account account = findAndAuthorize(accountNumber, userId);
        accountMapper.updateAccount(account, request);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    public void deleteAccount(String accountNumber, String userId) {
        log.info("Deleting account accountNumber={}", accountNumber);
        Account account = findAndAuthorize(accountNumber, userId);
        accountRepository.delete(account);
        log.info("Account deleted accountNumber={}", accountNumber);
    }

    private Account findAndAuthorize(String accountNumber, String userId) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (!account.getUserId().equals(userId)) {
            log.warn("Forbidden access to accountNumber={} by userId={}", accountNumber, userId);
            throw new ForbiddenException("Access denied");
        }
        return account;
    }
}

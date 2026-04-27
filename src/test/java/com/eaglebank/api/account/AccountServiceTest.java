package com.eaglebank.api.account;

import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock AccountMapper accountMapper;
    @Mock PrefixIdGenerator idGenerator;
    @InjectMocks AccountService accountService;

    private static final String USER_ID = "usr-abc123";
    private static final String ACCOUNT_NUMBER = "01234567";

    private Account accountFor(String accountNumber, String userId) {
        Account e = new Account();
        e.setAccountNumber(accountNumber);
        e.setUserId(userId);
        e.setBalance(BigDecimal.ZERO);
        return e;
    }

    private AccountResponse response() {
        return new AccountResponse(ACCOUNT_NUMBER, "10-10-10", "Personal Account",
                AccountType.personal, BigDecimal.ZERO, "GBP", Instant.now(), Instant.now());
    }

    @Test
    void createAccount_savesAndReturnsResponse() {
        Account account = accountFor(ACCOUNT_NUMBER, USER_ID);
        when(idGenerator.accountNumber()).thenReturn(ACCOUNT_NUMBER);
        when(accountMapper.toAccount(any(), any(), any())).thenReturn(account);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(response());

        AccountResponse result = accountService.createAccount(
                new CreateAccountRequest("Personal Account", AccountType.personal), USER_ID);

        assertThat(result.accountNumber()).isEqualTo(ACCOUNT_NUMBER);
    }

    @Test
    void listAccounts_returnsOnlyUserAccounts() {
        Account account = accountFor(ACCOUNT_NUMBER, USER_ID);
        when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(response());

        ListAccountsResponse result = accountService.listAccounts(USER_ID);

        assertThat(result.accounts()).hasSize(1);
    }

    @Test
    void fetchAccount_notFound_throws404() {
        when(accountRepository.findById("01999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.fetchAccount("01999999", USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void fetchAccount_differentOwner_throws403() {
        Account account = accountFor(ACCOUNT_NUMBER, "usr-other");
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.fetchAccount(ACCOUNT_NUMBER, USER_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteAccount_owner_deletesEntity() {
        Account account = accountFor(ACCOUNT_NUMBER, USER_ID);
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        accountService.deleteAccount(ACCOUNT_NUMBER, USER_ID);

        verify(accountRepository).delete(account);
    }
}

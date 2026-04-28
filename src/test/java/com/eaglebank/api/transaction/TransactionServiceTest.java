package com.eaglebank.api.transaction;

import com.eaglebank.api.account.Account;
import com.eaglebank.api.account.AccountRepository;
import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.error.UnprocessableContentException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock TransactionMapper transactionMapper;
    @Mock AccountRepository accountRepository;
    @Mock PrefixIdGenerator idGenerator;
    @InjectMocks TransactionService transactionService;

    private static final String USER_ID = "usr-abc123";
    private static final String ACCOUNT_NUMBER = "01234567";
    private static final String TRANSACTION_ID = "tan-abc123";

    private Account accountFor(String userId, BigDecimal balance) {
        Account a = new Account();
        a.setAccountNumber(ACCOUNT_NUMBER);
        a.setUserId(userId);
        a.setBalance(balance);
        return a;
    }

    private TransactionResponse response(TransactionType type) {
        return new TransactionResponse(TRANSACTION_ID, new BigDecimal("100.00"),
                "GBP", type, null, USER_ID, Instant.now());
    }

    @Test
    void createTransaction_deposit_updatesBalanceAndSaves() {
        Account account = accountFor(USER_ID, BigDecimal.ZERO);
        Transaction transaction = new Transaction();

        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(idGenerator.transactionId()).thenReturn(TRANSACTION_ID);
        when(transactionMapper.toTransaction(any(), any(), any(), any())).thenReturn(transaction);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(response(TransactionType.deposit));

        TransactionResponse result = transactionService.createTransaction(
                ACCOUNT_NUMBER, new CreateTransactionRequest(new BigDecimal("100.00"), TransactionType.deposit, null), USER_ID);

        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        assertThat(result.type()).isEqualTo(TransactionType.deposit);
    }

    @Test
    void createTransaction_withdrawal_updatesBalanceAndSaves() {
        Account account = accountFor(USER_ID, new BigDecimal("300.00"));
        Transaction transaction = new Transaction();

        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(idGenerator.transactionId()).thenReturn(TRANSACTION_ID);
        when(transactionMapper.toTransaction(any(), any(), any(), any())).thenReturn(transaction);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(response(TransactionType.withdrawal));

        TransactionResponse result = transactionService.createTransaction(
                ACCOUNT_NUMBER, new CreateTransactionRequest(new BigDecimal("100.00"), TransactionType.withdrawal, null), USER_ID);

        assertThat(account.getBalance()).isEqualByComparingTo("200.00");
        assertThat(result.type()).isEqualTo(TransactionType.withdrawal);
    }

    @Test
    void createTransaction_withdrawal_insufficientFunds_throws422() {
        Account account = accountFor(USER_ID, new BigDecimal("50.00"));
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.createTransaction(
                ACCOUNT_NUMBER,
                new CreateTransactionRequest(new BigDecimal("100.00"), TransactionType.withdrawal, null),
                USER_ID))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void createTransaction_deposit_exceedsMaxBalance_throws422() {
        Account account = accountFor(USER_ID, new BigDecimal("9500.00"));
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.createTransaction(
                ACCOUNT_NUMBER,
                new CreateTransactionRequest(new BigDecimal("600.00"), TransactionType.deposit, null),
                USER_ID))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessageContaining("10000.00");
    }

    @Test
    void createTransaction_accountNotFound_throws404() {
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(
                ACCOUNT_NUMBER,
                new CreateTransactionRequest(new BigDecimal("100.00"), TransactionType.deposit, null),
                USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createTransaction_differentOwner_throws403() {
        Account account = accountFor("usr-other", BigDecimal.ZERO);
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.createTransaction(
                ACCOUNT_NUMBER,
                new CreateTransactionRequest(new BigDecimal("100.00"), TransactionType.deposit, null),
                USER_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void fetchTransaction_wrongAccount_throws404() {
        Account account = accountFor(USER_ID, BigDecimal.ZERO);
        when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(transactionRepository.findByIdAndAccountNumber(TRANSACTION_ID, ACCOUNT_NUMBER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Transaction not found");
    }
}

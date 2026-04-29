package com.eaglebank.api.transaction;

import com.eaglebank.api.account.Account;
import com.eaglebank.api.account.AccountRepository;
import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.error.UnprocessableContentException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final BigDecimal MAX_BALANCE = new BigDecimal("10000.00");

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final PrefixIdGenerator idGenerator;

    @Transactional
    public TransactionResponse createTransaction(String accountNumber,
                                                 CreateTransactionRequest request,
                                                 String userId) {
        log.info("Transaction initiated accountNumber={} type={} amount={}", accountNumber, request.type(), request.amount());
        Account account = findAndAuthorizeAccount(accountNumber, userId);

        BigDecimal newBalance = switch (request.type()) {
            case deposit -> {
                BigDecimal result = account.getBalance().add(request.amount());
                if (result.compareTo(MAX_BALANCE) > 0) {
                    log.warn("Transaction rejected - max balance exceeded accountNumber={} currentBalance={} depositAmount={}",
                            accountNumber, account.getBalance(), request.amount());
                    throw new UnprocessableContentException("Deposit would exceed maximum balance of 10000.00");
                }
                yield result;
            }
            case withdrawal -> {
                BigDecimal result = account.getBalance().subtract(request.amount());
                if (result.compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("Transaction rejected - insufficient funds accountNumber={} currentBalance={} withdrawalAmount={}",
                            accountNumber, account.getBalance(), request.amount());
                    throw new UnprocessableContentException("Insufficient funds");
                }
                yield result;
            }
        };

        account.setBalance(newBalance);
        account.setUpdatedTimestamp(Instant.now());
        accountRepository.save(account);

        Transaction transaction = transactionMapper.toTransaction(
                request, idGenerator.transactionId(), accountNumber, userId);
        TransactionResponse response = transactionMapper.toResponse(transactionRepository.save(transaction));
        log.info("Transaction created transactionId={} accountNumber={} type={} amount={} newBalance={}",
                response.id(), accountNumber, request.type(), request.amount(), newBalance);
        return response;
    }

    public ListTransactionsResponse listTransactions(String accountNumber, String userId) {
        log.debug("Listing transactions accountNumber={}", accountNumber);
        findAndAuthorizeAccount(accountNumber, userId);
        List<TransactionResponse> transactions = transactionRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
        return new ListTransactionsResponse(transactions);
    }

    public TransactionResponse fetchTransaction(String accountNumber, String transactionId, String userId) {
        log.debug("Fetching transaction transactionId={} accountNumber={}", transactionId, accountNumber);
        findAndAuthorizeAccount(accountNumber, userId);
        Transaction transaction = transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }

    private Account findAndAuthorizeAccount(String accountNumber, String userId) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (!account.getUserId().equals(userId)) {
            log.warn("Forbidden access to accountNumber={} by userId={}", accountNumber, userId);
            throw new ForbiddenException("Access denied");
        }
        return account;
    }
}

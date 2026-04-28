package com.eaglebank.api.transaction;

import com.eaglebank.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable String accountNumber,
            @RequestBody @Valid CreateTransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(accountNumber, request, principal.userId()));
    }

    @GetMapping
    public ResponseEntity<ListTransactionsResponse> listTransactions(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(transactionService.listTransactions(accountNumber, principal.userId()));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> fetchTransaction(
            @PathVariable String accountNumber,
            @PathVariable String transactionId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(transactionService.fetchTransaction(accountNumber, transactionId, principal.userId()));
    }
}

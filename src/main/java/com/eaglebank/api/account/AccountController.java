package com.eaglebank.api.account;

import com.eaglebank.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid CreateAccountRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request, principal.userId()));
    }

    @GetMapping
    public ResponseEntity<ListAccountsResponse> listAccounts(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(accountService.listAccounts(principal.userId()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> fetchAccount(@PathVariable String accountNumber,
                                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(accountService.fetchAccount(accountNumber, principal.userId()));
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountNumber,
                                                         @RequestBody @Valid UpdateAccountRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, request, principal.userId()));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber,
                                              @AuthenticationPrincipal AuthenticatedUser principal) {
        accountService.deleteAccount(accountNumber, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
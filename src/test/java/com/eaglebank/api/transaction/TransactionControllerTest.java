package com.eaglebank.api.transaction;

import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.error.UnprocessableContentException;
import com.eaglebank.api.config.SecurityConfig;
import com.eaglebank.api.security.AuthenticatedUser;
import com.eaglebank.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TransactionService transactionService;
    @MockitoBean JwtService jwtService;

    private static final String USER_ID = "usr-abc123";
    private static final String ACCOUNT_NUMBER = "01234567";
    private static final String TRANSACTION_ID = "tan-abc123";

    private static final TransactionResponse TXN_RESPONSE = new TransactionResponse(
            TRANSACTION_ID, new BigDecimal("100.00"), "GBP",
            TransactionType.deposit, null, USER_ID, Instant.now());

    private UsernamePasswordAuthenticationToken authAs(String userId) {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId), null, List.of());
    }

    @Test
    void createTransaction_deposit_returns201() throws Exception {
        when(transactionService.createTransaction(eq(ACCOUNT_NUMBER), any(), eq(USER_ID)))
                .thenReturn(TXN_RESPONSE);

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100.00, "type": "deposit" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TRANSACTION_ID))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }

    @Test
    void createTransaction_insufficientFunds_returns422() throws Exception {
        when(transactionService.createTransaction(eq(ACCOUNT_NUMBER), any(), eq(USER_ID)))
                .thenThrow(new UnprocessableContentException("Insufficient funds"));

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 9999.99, "type": "withdrawal" }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void createTransaction_missingAmount_returns400() throws Exception {
        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "deposit" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_noToken_returns401() throws Exception {
        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100.00, "type": "deposit" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTransaction_accountBelongsToOtherUser_returns403() throws Exception {
        when(transactionService.createTransaction(eq(ACCOUNT_NUMBER), any(), eq("usr-other")))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .with(authentication(authAs("usr-other")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100.00, "type": "deposit" }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTransactions_returns200() throws Exception {
        when(transactionService.listTransactions(ACCOUNT_NUMBER, USER_ID))
                .thenReturn(new ListTransactionsResponse(List.of(TXN_RESPONSE)));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value(TRANSACTION_ID));
    }

    @Test
    void fetchTransaction_happyPath_returns200() throws Exception {
        when(transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                .thenReturn(TXN_RESPONSE);

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}",
                        ACCOUNT_NUMBER, TRANSACTION_ID)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRANSACTION_ID));
    }

    @Test
    void fetchTransaction_wrongAccount_returns404() throws Exception {
        when(transactionService.fetchTransaction(eq("01999999"), eq(TRANSACTION_ID), eq(USER_ID)))
                .thenThrow(new NotFoundException("Transaction not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}",
                        "01999999", TRANSACTION_ID)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }
}

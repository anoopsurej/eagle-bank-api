package com.eaglebank.api.account;

import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
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

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AccountService accountService;
    @MockitoBean JwtService jwtService;

    private static final String USER_ID = "usr-abc123";
    private static final String ACCOUNT_NUMBER = "01234567";

    private static final AccountResponse ACCOUNT_RESPONSE = new AccountResponse(
            ACCOUNT_NUMBER, "10-10-10", "Personal Account",
            AccountType.personal, BigDecimal.ZERO, "GBP",
            Instant.now(), Instant.now());

    private UsernamePasswordAuthenticationToken authAs(String userId) {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId), null, List.of());
    }

    @Test
    void createAccount_happyPath_returns201() throws Exception {
        when(accountService.createAccount(any(), eq(USER_ID))).thenReturn(ACCOUNT_RESPONSE);

        mockMvc.perform(post("/v1/accounts")
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Personal Account", "accountType": "personal" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.sortCode").value("10-10-10"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void createAccount_missingName_returns400() throws Exception {
        mockMvc.perform(post("/v1/accounts")
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "accountType": "personal" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_noToken_returns401() throws Exception {
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Personal Account", "accountType": "personal" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAccounts_returns200WithList() throws Exception {
        when(accountService.listAccounts(USER_ID))
                .thenReturn(new ListAccountsResponse(List.of(ACCOUNT_RESPONSE)));

        mockMvc.perform(get("/v1/accounts")
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value(ACCOUNT_NUMBER));
    }


    @Test
    void fetchAccount_happyPath_returns200() throws Exception {
        when(accountService.fetchAccount(ACCOUNT_NUMBER, USER_ID)).thenReturn(ACCOUNT_RESPONSE);

        mockMvc.perform(get("/v1/accounts/{accountNumber}", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER));
    }

    @Test
    void fetchAccount_differentUser_returns403() throws Exception {
        when(accountService.fetchAccount(eq(ACCOUNT_NUMBER), eq("usr-other")))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}", ACCOUNT_NUMBER)
                        .with(authentication(authAs("usr-other"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void fetchAccount_notFound_returns404() throws Exception {
        when(accountService.fetchAccount(eq("01999999"), eq(USER_ID)))
                .thenThrow(new NotFoundException("Account not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}", "01999999")
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteAccount_happyPath_returns204() throws Exception {
        mockMvc.perform(delete("/v1/accounts/{accountNumber}", ACCOUNT_NUMBER)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isNoContent());
    }
}

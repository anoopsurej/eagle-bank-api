package com.eaglebank.api.user;

import com.eaglebank.api.common.error.ConflictException;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private static final String USER_ID = "usr-abc123";

    private static final AddressDto ADDRESS = new AddressDto(
            "1 Main Street", null, null, "London", "Greater London", "SW1A 1AA");

    private static final UserResponse USER_RESPONSE = new UserResponse(
            USER_ID, "Test User", ADDRESS,
            "+441234567890", "test@example.com",
            Instant.now(), Instant.now());

    private UsernamePasswordAuthenticationToken authAs(String userId) {
        AuthenticatedUser principal = new AuthenticatedUser(userId);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    @Test
    void createUser_happyPath_returns201() throws Exception {
        when(userService.createUser(any())).thenReturn(USER_RESPONSE);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test User",
                                    "address": {
                                        "line1": "1 Main Street",
                                        "town": "London",
                                        "county": "Greater London",
                                        "postcode": "SW1A 1AA"
                                    },
                                    "phoneNumber": "+441234567890",
                                    "email": "test@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_missingName_returns400() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "address": {
                                        "line1": "1 Main Street",
                                        "town": "London",
                                        "county": "Greater London",
                                        "postcode": "SW1A 1AA"
                                    },
                                    "phoneNumber": "+441234567890",
                                    "email": "test@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("name"));
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test User",
                                    "address": {
                                        "line1": "1 Main Street",
                                        "town": "London",
                                        "county": "Greater London",
                                        "postcode": "SW1A 1AA"
                                    },
                                    "phoneNumber": "+441234567890",
                                    "email": "not-an-email",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("email"));
    }

    @Test
    void createUser_invalidPhoneNumber_returns400() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test User",
                                    "address": {
                                        "line1": "1 Main Street",
                                        "town": "London",
                                        "county": "Greater London",
                                        "postcode": "SW1A 1AA"
                                    },
                                    "phoneNumber": "07700900000",
                                    "email": "test@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("phoneNumber"));
    }

    @Test
    void createUser_emailAlreadyExists_returns409() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ConflictException("A user with this email already exists"));

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {                                                                                                                                                                                                                                                          \s
                                  "name": "Test User",
                                  "address": {                                                                                                                                                                                                                                           \s
                                      "line1": "1 Main Street",
                                      "town": "London",                                                                                                                                                                                                                                  \s
                                      "county": "Greater London",
                                      "postcode": "SW1A 1AA"                                                                                                                                                                                                                             \s
                                  },                                                                                                                                                                                                                                                     \s
                                  "phoneNumber": "+441234567890",
                                  "email": "test@example.com",                                                                                                                                                                                                                           \s
                                  "password": "password123"                                                                                                                                                                                                                              \s
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user with this email already exists"));
    }

    @Test
    void fetchUser_happyPath_returns200() throws Exception {
        when(userService.fetchUser(USER_ID, USER_ID)).thenReturn(USER_RESPONSE);

        mockMvc.perform(get("/v1/users/{id}", USER_ID)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void fetchUser_noToken_returns401() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fetchUser_differentUser_returns403() throws Exception {
        when(userService.fetchUser(eq(USER_ID), eq("usr-other")))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/v1/users/{id}", USER_ID)
                        .with(authentication(authAs("usr-other"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void fetchUser_notFound_returns404() throws Exception {
        when(userService.fetchUser(eq("usr-unknown"), eq(USER_ID)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/v1/users/{id}", "usr-unknown")
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }


    @Test
    void updateUser_happyPath_returns200() throws Exception {
        when(userService.updateUser(eq(USER_ID), any(), eq(USER_ID))).thenReturn(USER_RESPONSE);

        mockMvc.perform(patch("/v1/users/{id}", USER_ID)
                        .with(authentication(authAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Updated Name" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }


    @Test
    void deleteUser_happyPath_returns204() throws Exception {
        mockMvc.perform(delete("/v1/users/{id}", USER_ID)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_hasAccounts_returns409() throws Exception {
        doThrow(new ConflictException("User has active accounts"))
                .when(userService).deleteUser(eq(USER_ID), eq(USER_ID));

        mockMvc.perform(delete("/v1/users/{id}", USER_ID)
                        .with(authentication(authAs(USER_ID))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User has active accounts"));
    }
}

package com.eaglebank.api.user;

import com.eaglebank.api.config.SecurityConfig;
import com.eaglebank.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private static final AddressDto ADDRESS = new AddressDto(
            "1 Main Street", null, null, "London", "Greater London", "SW1A 1AA");

    private static final UserResponse USER_RESPONSE = new UserResponse(
            "usr-abc123", "Test User", ADDRESS,
            "+441234567890", "test@example.com",
            Instant.now(), Instant.now());

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
                .andExpect(jsonPath("$.id").value("usr-abc123"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
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

    // TODO: Email already exists
}

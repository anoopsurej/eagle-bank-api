package com.eaglebank.api.user;

import com.eaglebank.api.common.id.PrefixIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock PrefixIdGenerator idGenerator;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private static final AddressDto ADDRESS = new AddressDto(
            "1 Main Street", null, null, "London", "Greater London", "SW1A 1AA");

    private static final CreateUserRequest REQUEST = new CreateUserRequest(
            "Test User", ADDRESS, "+441234567890", "test@example.com", "password123");

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(REQUEST))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("email");
    }

    @Test
    void createUser_newUser_savesAndReturnsResponse() {
        User savedEntity = new User();
        UserResponse expectedResponse = new UserResponse(
                "usr-abc123", "Test User", ADDRESS,
                "+441234567890", "test@example.com",
                Instant.now(), Instant.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(idGenerator.userId()).thenReturn("usr-abc123");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userMapper.toUser(any(), anyString(), anyString())).thenReturn(savedEntity);
        when(userRepository.save(any())).thenReturn(savedEntity);
        when(userMapper.toResponse(any())).thenReturn(expectedResponse);

        UserResponse result = userService.createUser(REQUEST);

        assertThat(result.id()).isEqualTo("usr-abc123");
        assertThat(result.email()).isEqualTo("test@example.com");
    }
}

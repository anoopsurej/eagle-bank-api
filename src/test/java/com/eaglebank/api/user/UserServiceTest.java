package com.eaglebank.api.user;

import com.eaglebank.api.account.AccountRepository;
import com.eaglebank.api.common.error.ConflictException;
import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock PrefixIdGenerator idGenerator;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;
    @Mock AccountRepository accountRepository;

    private static final AddressDto ADDRESS = new AddressDto(
            "1 Main Street", null, null, "London", "Greater London", "SW1A 1AA");

    private static final CreateUserRequest REQUEST = new CreateUserRequest(
            "Test User", ADDRESS, "+441234567890", "test@example.com", "password123");

    private User userWithId(String id) {
        User e = new User();
        e.setId(id);
        return e;
    }

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(REQUEST))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("email");
    }

    @Test
    void createUser_newUser_savesAndReturnsResponse() {
        User savedUser = userWithId("usr-abc123");
        UserResponse expectedResponse = new UserResponse("usr-abc123", "Test User", ADDRESS,
                "+441234567890", "test@example.com", Instant.now(), Instant.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(idGenerator.userId()).thenReturn("usr-abc123");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userMapper.toUser(any(), anyString(), anyString())).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userMapper.toResponse(any())).thenReturn(expectedResponse);

        UserResponse result = userService.createUser(REQUEST);
        assertThat(result.id()).isEqualTo("usr-abc123");
        assertThat(result.email()).isEqualTo("test@example.com");
    }


    @Test
    void fetchUser_notFound_throws404() {
        when(userRepository.findById("usr-gone")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.fetchUser("usr-gone", "usr-gone"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void fetchUser_differentOwner_throws403() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(userWithId("usr-abc123")));

        assertThatThrownBy(() -> userService.fetchUser("usr-abc123", "usr-other"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void fetchUser_owner_returnsResponse() {
        User user = userWithId("usr-abc123");
        UserResponse expected = new UserResponse("usr-abc123", "Test User", ADDRESS,
                "+441234567890", "test@example.com", Instant.now(), Instant.now());

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        assertThat(userService.fetchUser("usr-abc123", "usr-abc123").id()).isEqualTo("usr-abc123");
    }


    @Test
    void deleteUser_notFound_throws404() {
        when(userRepository.findById("usr-gone")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("usr-gone", "usr-gone"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUser_differentOwner_throws403() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(userWithId("usr-abc123")));

        assertThatThrownBy(() -> userService.deleteUser("usr-abc123", "usr-other"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteUser_owner_deletesEntity() {
        User user = userWithId("usr-abc123");
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(user));
        when(accountRepository.existsByUserId("usr-abc123")).thenReturn(false);

        userService.deleteUser("usr-abc123", "usr-abc123");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_owner_withActiveAccounts_throws409() {
        User user = userWithId("usr-abc123");
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(user));
        when(accountRepository.existsByUserId("usr-abc123")).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser("usr-abc123", "usr-abc123"))
                .isInstanceOf(ConflictException.class);
    }
}

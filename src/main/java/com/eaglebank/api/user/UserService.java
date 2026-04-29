package com.eaglebank.api.user;

import com.eaglebank.api.account.AccountRepository;
import com.eaglebank.api.common.error.ConflictException;
import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PrefixIdGenerator idGenerator;
    private final AccountRepository accountRepository;

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user");
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("User creation conflict - email already exists");
            throw new ConflictException("A user with this email already exists");
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, idGenerator.userId(), hashedPassword);
        UserResponse response = userMapper.toResponse(userRepository.save(user));
        log.info("User created userId={}", response.id());
        return response;
    }

    public UserResponse fetchUser(String userId, String authenticatedUserId) {
        log.debug("Fetching user userId={}", userId);
        return userMapper.toResponse(findAndAuthorize(userId, authenticatedUserId));
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request, String authenticatedUserId) {
        log.info("Updating user userId={}", userId);
        User user = findAndAuthorize(userId, authenticatedUserId);
        userMapper.updateUser(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(String userId, String authenticatedUserId) {
        log.info("Deleting user userId={}", userId);
        User user = findAndAuthorize(userId, authenticatedUserId);
        if (accountRepository.existsByUserId(userId)) {
            log.warn("User deletion blocked - active accounts exist userId={}", userId);
            throw new ConflictException("User cannot be deleted while they have active accounts");
        }
        userRepository.delete(user);
        log.info("User deleted userId={}", userId);
    }

    private User findAndAuthorize(String userId, String authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getId().equals(authenticatedUserId)) {
            log.warn("Forbidden access to userId={} by authenticatedUserId={}", userId, authenticatedUserId);
            throw new ForbiddenException("Access denied");
        }
        return user;
    }
}

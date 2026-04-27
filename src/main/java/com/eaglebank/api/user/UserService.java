package com.eaglebank.api.user;

import com.eaglebank.api.account.AccountRepository;
import com.eaglebank.api.common.error.ConflictException;
import com.eaglebank.api.common.error.ForbiddenException;
import com.eaglebank.api.common.error.NotFoundException;
import com.eaglebank.api.common.id.PrefixIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PrefixIdGenerator idGenerator;
    private final AccountRepository accountRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("A user with this email already exists");
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, idGenerator.userId(), hashedPassword);
        return userMapper.toResponse(userRepository.save(user));
    }


    public UserResponse fetchUser(String userId, String authenticatedUserId) {
        User user = findAndAuthorize(userId, authenticatedUserId);
        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request, String authenticatedUserId) {
        User user = findAndAuthorize(userId, authenticatedUserId);
        userMapper.updateUser(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(String userId, String authenticatedUserId) {
        User user = findAndAuthorize(userId, authenticatedUserId);
        if (accountRepository.existsByUserId(userId)) {
            throw new ConflictException("User cannot be deleted while they have active accounts");
        }
        userRepository.delete(user);
    }

    private User findAndAuthorize(String userId, String authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("Access denied");
        }
        return user;
    }
}

package com.eaglebank.api.user;

import com.eaglebank.api.common.error.ConflictException;
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

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("A user with this email already exists");
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User entity = userMapper.toUser(request, idGenerator.userId(), hashedPassword);
        return userMapper.toResponse(userRepository.save(entity));
    }
}

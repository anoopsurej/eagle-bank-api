package com.eaglebank.api.auth;

import com.eaglebank.api.common.error.UnauthorizedException;
import com.eaglebank.api.security.JwtService;
import com.eaglebank.api.user.User;
import com.eaglebank.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt");

        Optional<User> maybeUser = userRepository.findByEmail(request.email());
        if (maybeUser.isEmpty()) {
            log.warn("Login failed - user not found");
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = maybeUser.get();
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed - invalid password userId={}", user.getId());
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId());
        log.info("Login successful userId={}", user.getId());
        return new LoginResponse(token, jwtService.extractExpiry(token));
    }
}

package com.eaglebank.api.user;

import com.eaglebank.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> fetchUser(@PathVariable String userId,
                                                  @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userService.fetchUser(userId, principal.userId()));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId,
                                                   @RequestBody @Valid UpdateUserRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userService.updateUser(userId, request, principal.userId()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId,
                                           @AuthenticationPrincipal AuthenticatedUser principal) {
        userService.deleteUser(userId, principal.userId());
        return ResponseEntity.noContent().build();
    }
}

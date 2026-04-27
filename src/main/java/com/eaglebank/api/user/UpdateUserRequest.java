package com.eaglebank.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        String name,
        @Valid AddressDto address,
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,
        @Email String email
) {
}

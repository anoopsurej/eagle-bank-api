package com.eaglebank.api.user;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        AddressDto address,
        String phoneNumber,
        String email,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
}

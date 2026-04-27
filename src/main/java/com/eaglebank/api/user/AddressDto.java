package com.eaglebank.api.user;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank String line1,
        String line2,
        String line3,
        @NotBlank String town,
        @NotBlank String county,
        @NotBlank String postcode
) {
}

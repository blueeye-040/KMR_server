package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        @NotBlank String name,
        @Pattern(regexp = "^$|\\d{10}", message = "Phone must be 10 digits")
        String phone,
        String avatarUrl
) {}

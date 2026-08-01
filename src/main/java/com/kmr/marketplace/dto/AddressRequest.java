package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") String phone,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits") String pincode,
        boolean isDefault,
        String type
) {}

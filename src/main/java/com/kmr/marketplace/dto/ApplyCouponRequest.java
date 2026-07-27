package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyCouponRequest(
        @NotBlank String code,
        double cartTotal
) {}

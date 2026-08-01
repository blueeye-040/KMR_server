package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Admin create/update payload for a coupon. */
public record CouponRequest(
        @NotBlank String code,
        String description,
        @Pattern(regexp = "FLAT|PERCENT", message = "type must be FLAT or PERCENT")
        String type,
        @Positive double value,
        double minCartValue,
        Double maxDiscount,
        Boolean active,
        String expiresAt,      // ISO-8601 instant, optional
        Integer usageLimit
) {}

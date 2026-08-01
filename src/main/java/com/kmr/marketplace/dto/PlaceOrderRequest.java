package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PlaceOrderRequest(
        @NotNull Long addressId,
        @NotNull @Pattern(regexp = "ONLINE|COD", message = "paymentMethod must be ONLINE or COD")
        String paymentMethod,
        String couponCode
) {}

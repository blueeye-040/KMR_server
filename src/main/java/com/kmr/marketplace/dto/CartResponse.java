package com.kmr.marketplace.dto;

import java.util.List;

public record CartResponse(
        List<CartItemDto> items,
        int    totalItems,
        double subtotal,
        double totalSavings,
        double deliveryCharge,
        double totalAmount
) {}

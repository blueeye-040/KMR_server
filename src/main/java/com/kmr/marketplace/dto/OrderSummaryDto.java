package com.kmr.marketplace.dto;

public record OrderSummaryDto(
        Long   id,
        String orderNumber,
        String status,
        String paymentMethod,
        String paymentStatus,
        double totalAmount,
        int    itemCount,
        String firstItemImage,
        String createdAt
) {}

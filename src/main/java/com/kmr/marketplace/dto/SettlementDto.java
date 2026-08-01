package com.kmr.marketplace.dto;

public record SettlementDto(
        Long   id,
        Long   orderId,
        Long   shopId,
        double amount,
        String mode,
        String status,
        String razorpayTransferId,
        String createdAt,
        String releasedAt
) {}

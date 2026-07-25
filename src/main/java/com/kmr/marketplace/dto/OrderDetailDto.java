package com.kmr.marketplace.dto;

import java.util.List;

public record OrderDetailDto(
        Long   id,
        String orderNumber,
        String status,
        String paymentMethod,
        String paymentStatus,
        double subtotal,
        double deliveryCharge,
        double discountAmount,
        double totalAmount,
        AddressDto shippingAddress,
        List<OrderItemDto> items,
        // Payment routing breakdown: each shop's payout + the platform delivery fee.
        List<ShopSettlementDto> shopSettlements,
        double platformDeliveryEarning,
        List<String> statusTimeline,
        String createdAt
) {}

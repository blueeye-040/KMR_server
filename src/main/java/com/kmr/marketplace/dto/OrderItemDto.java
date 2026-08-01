package com.kmr.marketplace.dto;

public record OrderItemDto(
        Long   productId,
        String name,
        String imageUrl,
        Long   shopId,
        String shopName,
        int    quantity,
        double price,
        double lineTotal,
        String status
) {}

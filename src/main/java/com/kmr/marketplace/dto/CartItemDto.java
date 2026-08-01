package com.kmr.marketplace.dto;

public record CartItemDto(
        Long   cartItemId,
        Long   shopProductId,
        Long   productId,
        String productName,
        String productImageUrl,
        String brandName,
        String shopName,
        double mrp,
        double sellingPrice,
        int    discountPercent,
        int    quantity,
        int    maxQuantity,       // min(stock, 10) — enforced cap
        int    deliveryDays,
        double itemTotal          // sellingPrice × quantity
) {}

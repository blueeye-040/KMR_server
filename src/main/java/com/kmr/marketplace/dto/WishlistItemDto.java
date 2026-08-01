package com.kmr.marketplace.dto;

public record WishlistItemDto(
        Long   productId,
        String name,
        String imageUrl,
        String brand,
        double mrp,
        double sellingPrice,
        int    discountPercent,
        double ratingAvg,
        int    reviewCount,
        boolean inStock
) {}

package com.kmr.marketplace.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long       id,
        String     name,
        String     slug,
        String     imageUrl,
        String     brandName,
        String     categoryName,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        int        discountPercent,
        int        deliveryDays,
        String     shopName,
        Long       shopId,
        double     ratingAvg,
        int        reviewCount
) {}

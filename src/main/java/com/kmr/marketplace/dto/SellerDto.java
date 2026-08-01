package com.kmr.marketplace.dto;

public record SellerDto(
        Long    shopProductId,
        Long    shopId,
        String  shopName,
        String  shopLogoUrl,
        double  shopRating,
        long    shopTotalSales,
        String  shopCity,
        double  mrp,
        double  sellingPrice,
        int     discountPercent,
        int     stock,
        int     deliveryDays,
        boolean isOfficial        // true for KMR Store
) {}

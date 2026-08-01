package com.kmr.marketplace.dto;

public record AnalyticsResponse(
        String role,
        long   totalProducts,
        long   totalCategories,
        long   totalShops,
        long   totalUsers,
        long   totalOrders,
        double totalRevenue
) {}

package com.kmr.marketplace.dto;

public record ReviewDto(
        Long   id,
        String userName,
        String userAvatarUrl,
        int    rating,
        String title,
        String body,
        boolean verifiedPurchase,
        int    helpfulCount,
        String createdAt
) {}

package com.kmr.marketplace.dto;

public record AddReviewRequest(
        int    rating,     // 1-5
        String title,
        String body
) {}

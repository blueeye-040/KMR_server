package com.kmr.marketplace.dto;

public record BannerDto(
        Long   id,
        String title,
        String subtitle,
        String imageUrl,
        String bgColorHex
) {}

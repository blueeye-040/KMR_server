package com.kmr.marketplace.dto;

public record CategoryDto(
        Long   id,
        String name,
        String emoji,
        String colorHex,
        String imageUrl
) {}

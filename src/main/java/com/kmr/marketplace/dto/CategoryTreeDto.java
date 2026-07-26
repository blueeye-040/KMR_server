package com.kmr.marketplace.dto;

import java.util.List;

/** A department (top-level category) with its shoppable child categories. */
public record CategoryTreeDto(
        Long   id,
        String name,
        String emoji,
        String colorHex,
        String imageUrl,
        List<CategoryDto> children
) {}

package com.kmr.marketplace.dto;

import java.util.List;

public record PagedProductResponse(
        List<ProductDto> content,
        int              page,
        int              size,
        long             totalElements,
        int              totalPages,
        boolean          last
) {}

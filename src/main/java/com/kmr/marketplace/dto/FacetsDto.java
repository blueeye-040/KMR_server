package com.kmr.marketplace.dto;

import java.util.List;

/** Available filter options for the current result set (drives the filter sheet). */
public record FacetsDto(
        List<BrandFacet> brands,
        double minPrice,
        double maxPrice
) {
    public record BrandFacet(Long id, String name, long count) {}
}

package com.kmr.marketplace.dto;

/**
 * Search / filter / sort criteria for the product listing.
 * All fields are optional; nulls mean "no constraint".
 * `sort` is one of: relevance, price_asc, price_desc, newest, popularity, rating, discount.
 */
public record ProductFilter(
        String  q,
        Long    categoryId,
        Long    departmentId,
        Long    brandId,
        Double  minPrice,
        Double  maxPrice,
        Double  minRating,
        Integer minDiscount,
        String  sort,
        int     page,
        int     size
) {}

package com.kmr.marketplace.dto;

import java.util.List;

public record HomeResponse(
        List<BannerDto>   banners,
        List<CategoryDto> categories,
        List<ProductDto>  dealsOfTheDay,
        List<ProductDto>  featured,
        List<ProductDto>  newArrivals,
        List<ProductDto>  topSelling,
        List<ProductDto>  recommended,
        String            greeting
) {}

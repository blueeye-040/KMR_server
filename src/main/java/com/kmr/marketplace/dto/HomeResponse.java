package com.kmr.marketplace.dto;

import java.util.List;

public record HomeResponse(
        List<BannerDto>   banners,
        List<CategoryDto> categories,
        List<ProductDto>  featured,
        List<ProductDto>  newArrivals,
        List<ProductDto>  topSelling,
        String            greeting
) {}

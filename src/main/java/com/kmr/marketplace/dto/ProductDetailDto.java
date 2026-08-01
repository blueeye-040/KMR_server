package com.kmr.marketplace.dto;

import java.util.List;

public record ProductDetailDto(
        // ── Identity ─────────────────────────────────────────
        Long   id,
        String name,
        String slug,
        String description,
        String imageUrl,
        List<String> galleryImages,

        // ── Classification ────────────────────────────────────
        String brandName,
        String brandLogoUrl,
        String categoryName,
        Long   categoryId,
        String specifications,       // raw JSONB string — Flutter parses

        boolean featured,
        boolean newArrival,
        boolean topSelling,

        // ── Best price (cheapest seller) ──────────────────────
        double sellingPrice,
        double mrp,
        int    discountPercent,
        int    stock,
        int    deliveryDays,
        String shopName,
        Long   shopId,
        double shopRating,
        Long   shopProductId,

        // ── All sellers ───────────────────────────────────────
        List<SellerDto> allSellers,

        // ── Reviews ───────────────────────────────────────────
        double ratingAvg,
        int    reviewCount,
        List<Integer> ratingBreakdown,   // [5★ count, 4★, 3★, 2★, 1★]
        List<ReviewDto> reviews,

        // ── Related products (same category) ─────────────────
        List<ProductDto> related
) {}

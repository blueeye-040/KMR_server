package com.kmr.marketplace.dto;

/**
 * One vendor's share of an order — the amount that settles to that shop's
 * bank account. The platform (app owner) keeps the delivery charge separately.
 */
public record ShopSettlementDto(
        Long   shopId,
        String shopName,
        boolean isOfficial,
        double amount
) {}

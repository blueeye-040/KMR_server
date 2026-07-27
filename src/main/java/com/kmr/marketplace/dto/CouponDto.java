package com.kmr.marketplace.dto;

/** Coupon as shown to users ("available offers") and to admins. */
public record CouponDto(
        Long    id,
        String  code,
        String  description,
        String  type,
        double  value,
        double  minCartValue,
        Double  maxDiscount,
        boolean active,
        String  expiresAt,
        Integer usageLimit,
        int     usedCount
) {}

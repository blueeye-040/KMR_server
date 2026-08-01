package com.kmr.marketplace.dto;

/** Result of previewing/applying a coupon against a cart total. */
public record CouponResult(
        boolean valid,
        String  code,
        double  discount,
        String  message
) {
    public static CouponResult invalid(String code, String message) {
        return new CouponResult(false, code, 0, message);
    }
}

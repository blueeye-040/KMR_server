package com.kmr.marketplace.dto;

public record VerifyPaymentRequest(
        String razorpayPaymentId,
        String razorpaySignature
) {}

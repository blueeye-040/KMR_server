package com.kmr.marketplace.dto;

/**
 * Returned right after an order is created.
 * For COD, the order is already placed (razorpay fields null).
 * For ONLINE, the app opens Razorpay checkout using razorpayOrderId + razorpayKeyId,
 * then calls POST /api/orders/{id}/payment/verify.
 */
public record PlaceOrderResponse(
        Long   orderId,
        String orderNumber,
        String paymentMethod,
        String paymentStatus,
        double amountPayable,
        String razorpayOrderId,
        String razorpayKeyId
) {}

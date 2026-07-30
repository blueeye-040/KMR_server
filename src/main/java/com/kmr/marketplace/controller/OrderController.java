package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.security.AuthHelper;
import com.kmr.marketplace.service.OrderService;
import com.kmr.marketplace.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final SettlementService settlementService;
    private final AuthHelper authHelper;

    public OrderController(OrderService orderService,
                          SettlementService settlementService,
                          AuthHelper authHelper) {
        this.orderService = orderService;
        this.settlementService = settlementService;
        this.authHelper = authHelper;
    }

    /** Place an order from the current cart. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest req) {
        return orderService.placeOrder(req);
    }

    /** Confirm an online payment (called after Razorpay checkout completes). */
    @PostMapping("/{id}/payment/verify")
    public OrderDetailDto verifyPayment(@PathVariable Long id,
                                        @RequestBody VerifyPaymentRequest req) {
        return orderService.verifyPayment(id, req);
    }

    @GetMapping
    public List<OrderSummaryDto> list() {
        return orderService.listOrders();
    }

    @GetMapping("/{id}")
    public OrderDetailDto detail(@PathVariable Long id) {
        return orderService.detail(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderDetailDto cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    /** Admin: advance delivery status (DELIVERED releases held vendor payouts). */
    @PatchMapping("/{id}/status")
    public OrderDetailDto updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderService.updateStatus(id, body.get("status"));
    }

    /** Admin: the per-vendor settlement records for an order. */
    @GetMapping("/{id}/settlements")
    public List<SettlementDto> settlements(@PathVariable Long id) {
        authHelper.requireRole(com.kmr.marketplace.entity.UserRole.ADMIN);
        return settlementService.forOrder(id);
    }
}

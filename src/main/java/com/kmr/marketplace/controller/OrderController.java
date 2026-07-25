package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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
}

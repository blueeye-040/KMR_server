package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.CartResponse;
import com.kmr.marketplace.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** GET /api/cart — full cart with price breakdown */
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    /** GET /api/cart/count — item count (for badge in app bar) */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> count() {
        return ResponseEntity.ok(Map.of("count", cartService.cartCount()));
    }

    /** POST /api/cart/{shopProductId}?quantity=1 — add or increase */
    @PostMapping("/{shopProductId}")
    public ResponseEntity<CartResponse> add(
            @PathVariable Long shopProductId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(cartService.addToCart(shopProductId, quantity));
    }

    /** PUT /api/cart/{cartItemId}?quantity=2 — set absolute quantity */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> update(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, quantity));
    }

    /** DELETE /api/cart/{cartItemId} — remove one item */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> remove(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(cartItemId));
    }

    /** DELETE /api/cart — clear entire cart */
    @DeleteMapping
    public ResponseEntity<Void> clear() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}

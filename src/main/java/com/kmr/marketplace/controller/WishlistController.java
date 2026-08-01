package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.WishlistItemDto;
import com.kmr.marketplace.service.WishlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistItemDto> list() {
        return wishlistService.list();
    }

    @GetMapping("/count")
    public Map<String, Integer> count() {
        return Map.of("count", wishlistService.count());
    }

    /** Toggle a product in the wishlist. Returns {"wishlisted": true|false}. */
    @PostMapping("/{productId}")
    public Map<String, Boolean> toggle(@PathVariable Long productId) {
        return Map.of("wishlisted", wishlistService.toggle(productId));
    }

    @DeleteMapping("/{productId}")
    public Map<String, Boolean> remove(@PathVariable Long productId) {
        wishlistService.remove(productId);
        return Map.of("wishlisted", false);
    }
}

package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ── User ────────────────────────────────────────────────────────────────

    /** Available offers a user can currently apply. */
    @GetMapping
    public List<CouponDto> available() {
        return couponService.available();
    }

    /** Preview a coupon against the current cart total. */
    @PostMapping("/apply")
    public CouponResult apply(@Valid @RequestBody ApplyCouponRequest req) {
        return couponService.preview(req.code(), req.cartTotal());
    }

    // ── Admin (role-restricted) ──────────────────────────────────────────────

    @GetMapping("/admin")
    public List<CouponDto> adminList() {
        return couponService.adminList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDto create(@Valid @RequestBody CouponRequest req) {
        return couponService.create(req);
    }

    @PutMapping("/{id}")
    public CouponDto update(@PathVariable Long id, @Valid @RequestBody CouponRequest req) {
        return couponService.update(id, req);
    }

    /** Enable/disable — controls whether the coupon is offered to users. */
    @PatchMapping("/{id}/active")
    public CouponDto setActive(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return couponService.setActive(id, Boolean.TRUE.equals(body.get("active")));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        couponService.delete(id);
    }
}

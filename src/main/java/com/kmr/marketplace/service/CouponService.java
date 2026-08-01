package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.CouponDto;
import com.kmr.marketplace.dto.CouponRequest;
import com.kmr.marketplace.dto.CouponResult;
import com.kmr.marketplace.entity.Coupon;
import com.kmr.marketplace.entity.UserRole;
import com.kmr.marketplace.repository.CouponRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private final CouponRepository couponRepo;
    private final AuthHelper authHelper;

    public CouponService(CouponRepository couponRepo, AuthHelper authHelper) {
        this.couponRepo = couponRepo;
        this.authHelper = authHelper;
    }

    // ── User-facing: available offers ───────────────────────────────────────────

    /** Active, non-expired, non-exhausted coupons a user can currently use. */
    public List<CouponDto> available() {
        Instant now = Instant.now();
        return couponRepo.findByActiveTrueOrderByMinCartValueAsc().stream()
                .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(now))
                .filter(c -> c.getUsageLimit() == null || c.getUsedCount() < c.getUsageLimit())
                .map(CouponService::toDto)
                .toList();
    }

    // ── Admin: management ───────────────────────────────────────────────────────

    public List<CouponDto> adminList() {
        authHelper.requireRole(UserRole.ADMIN);
        return couponRepo.findAllByOrderByIdDesc().stream().map(CouponService::toDto).toList();
    }

    @Transactional
    public CouponDto create(CouponRequest req) {
        authHelper.requireRole(UserRole.ADMIN);
        if (couponRepo.existsByCodeIgnoreCase(req.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A coupon with this code already exists");
        }
        Coupon c = new Coupon();
        apply(c, req);
        return toDto(couponRepo.save(c));
    }

    @Transactional
    public CouponDto update(Long id, CouponRequest req) {
        authHelper.requireRole(UserRole.ADMIN);
        Coupon c = couponRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        apply(c, req);
        return toDto(couponRepo.save(c));
    }

    /** Enable/disable a coupon — controls whether users can see & use it. */
    @Transactional
    public CouponDto setActive(Long id, boolean active) {
        authHelper.requireRole(UserRole.ADMIN);
        Coupon c = couponRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        c.setActive(active);
        return toDto(couponRepo.save(c));
    }

    @Transactional
    public void delete(Long id) {
        authHelper.requireRole(UserRole.ADMIN);
        couponRepo.deleteById(id);
    }

    private void apply(Coupon c, CouponRequest req) {
        c.setCode(req.code().trim().toUpperCase());
        c.setDescription(req.description());
        c.setType(req.type() == null ? "FLAT" : req.type());
        c.setValue(BigDecimal.valueOf(req.value()));
        c.setMinCartValue(BigDecimal.valueOf(req.minCartValue()));
        c.setMaxDiscount(req.maxDiscount() != null ? BigDecimal.valueOf(req.maxDiscount()) : null);
        c.setActive(req.active() == null || req.active());
        c.setUsageLimit(req.usageLimit());
        if (req.expiresAt() != null && !req.expiresAt().isBlank()) {
            try { c.setExpiresAt(Instant.parse(req.expiresAt())); }
            catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expiresAt must be ISO-8601");
            }
        }
    }

    static CouponDto toDto(Coupon c) {
        return new CouponDto(c.getId(), c.getCode(), c.getDescription(), c.getType(),
                c.getValue().doubleValue(),
                c.getMinCartValue() != null ? c.getMinCartValue().doubleValue() : 0,
                c.getMaxDiscount() != null ? c.getMaxDiscount().doubleValue() : null,
                c.isActive(),
                c.getExpiresAt() != null ? c.getExpiresAt().toString() : null,
                c.getUsageLimit(), c.getUsedCount());
    }

    /** Preview a coupon for a cart total (does not consume it). */
    public CouponResult preview(String code, double cartTotal) {
        Coupon c = couponRepo.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (c == null || !c.isActive()) {
            return CouponResult.invalid(code, "This coupon code is not valid.");
        }
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(Instant.now())) {
            return CouponResult.invalid(code, "This coupon has expired.");
        }
        if (c.getUsageLimit() != null && c.getUsedCount() >= c.getUsageLimit()) {
            return CouponResult.invalid(code, "This coupon is no longer available.");
        }
        double minCart = c.getMinCartValue() != null ? c.getMinCartValue().doubleValue() : 0;
        if (cartTotal < minCart) {
            double diff = minCart - cartTotal;
            return CouponResult.invalid(code,
                    "Add items worth ₹" + MONEY.format(diff) + " more to use this coupon.");
        }
        double discount = computeDiscount(c, cartTotal);
        String label = c.getDescription() != null ? c.getDescription()
                : "You saved ₹" + MONEY.format(discount);
        return new CouponResult(true, c.getCode(), discount, label);
    }

    /**
     * Validate + compute the discount at checkout. Returns the Coupon (to consume)
     * and the discount, or null discount if the code is blank/invalid.
     */
    @Transactional
    public Applied applyAtCheckout(String code, double cartTotal) {
        if (code == null || code.isBlank()) return new Applied(0, null);
        CouponResult r = preview(code, cartTotal);
        if (!r.valid()) return new Applied(0, null);
        Coupon c = couponRepo.findByCodeIgnoreCase(code.trim()).orElseThrow();
        c.setUsedCount(c.getUsedCount() + 1);   // consume
        return new Applied(r.discount(), c.getCode());
    }

    private double computeDiscount(Coupon c, double cartTotal) {
        double raw;
        if ("PERCENT".equalsIgnoreCase(c.getType())) {
            raw = cartTotal * c.getValue().doubleValue() / 100.0;
            if (c.getMaxDiscount() != null) {
                raw = Math.min(raw, c.getMaxDiscount().doubleValue());
            }
        } else { // FLAT
            raw = c.getValue().doubleValue();
        }
        return Math.min(raw, cartTotal); // never exceed the cart
    }

    /** Discount amount + the code that was consumed (null if none applied). */
    public record Applied(double discount, String code) {
        public BigDecimal discountBd() { return BigDecimal.valueOf(discount); }
    }
}

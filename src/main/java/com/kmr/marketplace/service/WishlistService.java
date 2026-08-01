package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.WishlistItemDto;
import com.kmr.marketplace.entity.Product;
import com.kmr.marketplace.entity.ShopProduct;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.entity.WishlistItem;
import com.kmr.marketplace.repository.ProductRepository;
import com.kmr.marketplace.repository.ShopProductRepository;
import com.kmr.marketplace.repository.WishlistRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepo;
    private final ProductRepository productRepo;
    private final ShopProductRepository shopProductRepo;
    private final AuthHelper authHelper;

    public WishlistService(WishlistRepository wishlistRepo,
                           ProductRepository productRepo,
                           ShopProductRepository shopProductRepo,
                           AuthHelper authHelper) {
        this.wishlistRepo    = wishlistRepo;
        this.productRepo     = productRepo;
        this.shopProductRepo = shopProductRepo;
        this.authHelper      = authHelper;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemDto> list() {
        User user = authHelper.currentUser();
        List<WishlistItem> items = wishlistRepo.findByUserIdWithProduct(user.getId());
        if (items.isEmpty()) return List.of();

        List<Long> productIds = items.stream().map(w -> w.getProduct().getId()).toList();
        // Best (cheapest available) shop price per product for display.
        Map<Long, ShopProduct> best = shopProductRepo.findByProductIds(new ArrayList<>(productIds))
                .stream().collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(), sp -> sp,
                        (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b));

        return items.stream().map(w -> {
            Product p = w.getProduct();
            ShopProduct sp = best.get(p.getId());
            return new WishlistItemDto(
                    p.getId(), p.getName(), p.getImageUrl(),
                    p.getBrand() != null ? p.getBrand().getName() : null,
                    sp != null ? sp.getMrp().doubleValue() : 0,
                    sp != null ? sp.getSellingPrice().doubleValue() : 0,
                    sp != null ? sp.getDiscountPercent() : 0,
                    p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0,
                    p.getReviewCount(),
                    sp != null && sp.getStock() > 0);
        }).toList();
    }

    public boolean toggle(Long productId) {
        User user = authHelper.currentUser();
        var existing = wishlistRepo.findByUserIdAndProductId(user.getId(), productId);
        if (existing.isPresent()) {
            wishlistRepo.delete(existing.get());
            return false;   // now not wishlisted
        }
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        WishlistItem w = new WishlistItem();
        w.setUser(user);
        w.setProduct(p);
        wishlistRepo.save(w);
        return true;        // now wishlisted
    }

    public void remove(Long productId) {
        User user = authHelper.currentUser();
        wishlistRepo.findByUserIdAndProductId(user.getId(), productId)
                .ifPresent(wishlistRepo::delete);
    }

    @Transactional(readOnly = true)
    public int count() {
        User user = authHelper.currentUserOrNull();
        return user != null ? wishlistRepo.countByUserId(user.getId()) : 0;
    }
}

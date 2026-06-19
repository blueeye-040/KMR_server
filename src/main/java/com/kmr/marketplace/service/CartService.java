package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.CartItemDto;
import com.kmr.marketplace.dto.CartResponse;
import com.kmr.marketplace.entity.*;
import com.kmr.marketplace.repository.CartRepository;
import com.kmr.marketplace.repository.ShopProductRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    private static final int MAX_QTY_PER_ITEM = 10;
    private static final double FREE_DELIVERY_THRESHOLD = 499.0;
    private static final double DELIVERY_CHARGE = 49.0;

    private final CartRepository         cartRepo;
    private final ShopProductRepository  shopProductRepo;
    private final AuthHelper             authHelper;

    public CartService(CartRepository cartRepo,
                       ShopProductRepository shopProductRepo,
                       AuthHelper authHelper) {
        this.cartRepo        = cartRepo;
        this.shopProductRepo = shopProductRepo;
        this.authHelper      = authHelper;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = authHelper.currentUser();
        return buildResponse(cartRepo.findByUserIdWithDetails(user.getId()));
    }

    public CartResponse addToCart(Long shopProductId, int qty) {
        User user = authHelper.currentUser();
        ShopProduct sp = shopProductRepo.findById(shopProductId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product listing not found"));

        if (!sp.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This product is currently unavailable");
        }
        if (sp.getStock() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Out of stock");
        }

        Optional<CartItem> existing =
                cartRepo.findByUserIdAndShopProductId(user.getId(), shopProductId);

        CartItem item;
        if (existing.isPresent()) {
            item = existing.get();
            item.setQuantity(clamp(item.getQuantity() + qty, sp.getStock()));
        } else {
            item = new CartItem();
            item.setUser(user);
            item.setShopProduct(sp);
            item.setQuantity(clamp(qty, sp.getStock()));
        }
        cartRepo.save(item);

        return getCart();
    }

    public CartResponse updateQuantity(Long cartItemId, int qty) {
        User user = authHelper.currentUser();
        CartItem item = findOwned(cartItemId, user.getId());

        if (qty <= 0) {
            cartRepo.delete(item);
        } else {
            item.setQuantity(clamp(qty, item.getShopProduct().getStock()));
            cartRepo.save(item);
        }
        return getCart();
    }

    public CartResponse removeItem(Long cartItemId) {
        User user = authHelper.currentUser();
        cartRepo.delete(findOwned(cartItemId, user.getId()));
        return getCart();
    }

    public void clearCart() {
        cartRepo.deleteByUserId(authHelper.currentUser().getId());
    }

    @Transactional(readOnly = true)
    public int cartCount() {
        User user = authHelper.currentUserOrNull();
        return user != null ? cartRepo.countByUserId(user.getId()) : 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CartItem findOwned(Long cartItemId, Long userId) {
        CartItem item = cartRepo.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart item not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return item;
    }

    private int clamp(int requested, int stock) {
        return Math.min(requested, Math.min(stock, MAX_QTY_PER_ITEM));
    }

    private CartResponse buildResponse(List<CartItem> items) {
        double subtotal       = 0;
        double totalSavings   = 0;
        List<CartItemDto> dtos = new ArrayList<>();

        for (CartItem ci : items) {
            ShopProduct sp = ci.getShopProduct();
            Product     p  = sp.getProduct();

            double price    = sp.getSellingPrice().doubleValue();
            double fullMrp  = sp.getMrp().doubleValue();
            double itemTotal = price * ci.getQuantity();
            double itemSaving = (fullMrp - price) * ci.getQuantity();

            subtotal     += itemTotal;
            totalSavings += itemSaving;

            dtos.add(new CartItemDto(
                    ci.getId(),
                    sp.getId(),
                    p.getId(),
                    p.getName(),
                    p.getImageUrl(),
                    p.getBrand() != null ? p.getBrand().getName() : null,
                    sp.getShop().getName(),
                    fullMrp,
                    price,
                    sp.getDiscountPercent(),
                    ci.getQuantity(),
                    Math.min(sp.getStock(), MAX_QTY_PER_ITEM),
                    sp.getDeliveryDays(),
                    itemTotal));
        }

        double delivery = (items.isEmpty() || subtotal >= FREE_DELIVERY_THRESHOLD)
                ? 0 : DELIVERY_CHARGE;

        return new CartResponse(dtos, dtos.size(), subtotal, totalSavings,
                delivery, subtotal + delivery);
    }
}

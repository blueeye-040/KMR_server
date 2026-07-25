package com.kmr.marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.*;
import com.kmr.marketplace.repository.AddressRepository;
import com.kmr.marketplace.repository.CartRepository;
import com.kmr.marketplace.repository.OrderRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private static final double FREE_DELIVERY_THRESHOLD = 499.0;
    private static final double DELIVERY_CHARGE          = 49.0;

    // Canonical forward status flow used to render the tracking timeline.
    private static final List<String> FLOW = List.of(
            "PLACED", "CONFIRMED", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED");

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository   orderRepo;
    private final CartRepository    cartRepo;
    private final AddressRepository addressRepo;
    private final PaymentService    paymentService;
    private final AuthHelper        authHelper;
    private final ObjectMapper      objectMapper;

    public OrderService(OrderRepository orderRepo,
                        CartRepository cartRepo,
                        AddressRepository addressRepo,
                        PaymentService paymentService,
                        AuthHelper authHelper,
                        ObjectMapper objectMapper) {
        this.orderRepo      = orderRepo;
        this.cartRepo       = cartRepo;
        this.addressRepo    = addressRepo;
        this.paymentService = paymentService;
        this.authHelper     = authHelper;
        this.objectMapper   = objectMapper;
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    public PlaceOrderResponse placeOrder(PlaceOrderRequest req) {
        User user = authHelper.currentUser();

        List<CartItem> cart = cartRepo.findByUserIdWithDetails(user.getId());
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        Address address = addressRepo.findByIdAndUserId(req.addressId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery address not found"));

        // Validate stock & compute subtotal.
        double subtotal = 0;
        for (CartItem ci : cart) {
            ShopProduct sp = ci.getShopProduct();
            if (!sp.isAvailable() || sp.getStock() < ci.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "'" + sp.getProduct().getName() + "' is out of stock or has insufficient quantity");
            }
            subtotal += sp.getSellingPrice().doubleValue() * ci.getQuantity();
        }

        double delivery = subtotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_CHARGE;
        double discount = 0;   // coupon handling comes later
        double total    = subtotal + delivery - discount;

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(BigDecimal.valueOf(total));
        order.setDeliveryCharge(BigDecimal.valueOf(delivery));
        order.setDiscountAmount(BigDecimal.valueOf(discount));
        order.setStatus("PLACED");
        order.setPaymentMethod(req.paymentMethod());
        order.setPaymentStatus("PENDING");
        order.setShippingAddress(writeAddressJson(address));

        for (CartItem ci : cart) {
            ShopProduct sp = ci.getShopProduct();
            OrderItem item = new OrderItem();
            item.setShopProduct(sp);
            item.setProduct(sp.getProduct());
            item.setShop(sp.getShop());
            item.setQuantity(ci.getQuantity());
            item.setPrice(sp.getSellingPrice());
            item.setStatus("PLACED");
            order.addItem(item);

            sp.setStock(sp.getStock() - ci.getQuantity());   // reserve stock
        }

        Order saved = orderRepo.save(order);
        cartRepo.deleteByUserId(user.getId());

        String razorpayOrderId = null;
        String razorpayKeyId   = null;
        if ("ONLINE".equals(req.paymentMethod())) {
            razorpayOrderId = paymentService.createOrder(saved.getTotalAmount(), saved.getOrderNumber());
            razorpayKeyId   = paymentService.keyId();
            saved.setRazorpayOrderId(razorpayOrderId);
        }

        return new PlaceOrderResponse(
                saved.getId(), saved.getOrderNumber(), saved.getPaymentMethod(),
                saved.getPaymentStatus(), total, razorpayOrderId, razorpayKeyId);
    }

    public OrderDetailDto verifyPayment(Long orderId, VerifyPaymentRequest req) {
        User user = authHelper.currentUser();
        Order order = orderRepo.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!"ONLINE".equals(order.getPaymentMethod())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not an online payment");
        }
        if ("PAID".equals(order.getPaymentStatus())) {
            return detail(orderId);   // idempotent
        }

        boolean ok = paymentService.verifySignature(
                order.getRazorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());
        if (!ok) {
            order.setPaymentStatus("FAILED");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        order.setPaymentStatus("PAID");
        order.setRazorpayPaymentId(req.razorpayPaymentId());
        order.setStatus("CONFIRMED");
        return detail(orderId);
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> listOrders() {
        User user = authHelper.currentUser();
        return orderRepo.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(o -> new OrderSummaryDto(
                        o.getId(), o.getOrderNumber(), o.getStatus(),
                        o.getPaymentMethod(), o.getPaymentStatus(),
                        o.getTotalAmount().doubleValue(),
                        o.getItems().size(),
                        o.getItems().isEmpty() ? null : o.getItems().get(0).getProduct().getImageUrl(),
                        o.getCreatedAt() != null ? o.getCreatedAt().toString() : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailDto detail(Long orderId) {
        User user = authHelper.currentUser();
        Order o = orderRepo.findDetailByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        List<OrderItemDto> itemDtos = o.getItems().stream().map(i -> {
            double lineTotal = i.getPrice().doubleValue() * i.getQuantity();
            return new OrderItemDto(
                    i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getImageUrl(),
                    i.getShop().getId(), i.getShop().getName(),
                    i.getQuantity(), i.getPrice().doubleValue(), lineTotal, i.getStatus());
        }).toList();

        // Payment routing: sum each shop's line totals → that shop's payout.
        Map<Long, ShopSettlementDto> byShop = new LinkedHashMap<>();
        for (OrderItem i : o.getItems()) {
            Shop shop = i.getShop();
            double line = i.getPrice().doubleValue() * i.getQuantity();
            byShop.merge(shop.getId(),
                    new ShopSettlementDto(shop.getId(), shop.getName(), shop.isOfficial(), line),
                    (a, b) -> new ShopSettlementDto(a.shopId(), a.shopName(), a.isOfficial(), a.amount() + b.amount()));
        }

        double subtotal = o.getItems().stream()
                .mapToDouble(i -> i.getPrice().doubleValue() * i.getQuantity()).sum();

        return new OrderDetailDto(
                o.getId(), o.getOrderNumber(), o.getStatus(),
                o.getPaymentMethod(), o.getPaymentStatus(),
                subtotal, o.getDeliveryCharge().doubleValue(),
                o.getDiscountAmount().doubleValue(), o.getTotalAmount().doubleValue(),
                readAddressJson(o.getShippingAddress()),
                itemDtos,
                new ArrayList<>(byShop.values()),
                o.getDeliveryCharge().doubleValue(),
                timeline(o.getStatus()),
                o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
    }

    public OrderDetailDto cancel(Long orderId) {
        User user = authHelper.currentUser();
        Order o = orderRepo.findDetailByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!List.of("PLACED", "CONFIRMED").contains(o.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order can no longer be cancelled");
        }
        o.setStatus("CANCELLED");
        for (OrderItem i : o.getItems()) {
            i.setStatus("CANCELLED");
            ShopProduct sp = i.getShopProduct();
            if (sp != null) sp.setStock(sp.getStock() + i.getQuantity());  // restock
        }
        if ("PAID".equals(o.getPaymentStatus())) o.setPaymentStatus("REFUNDED");
        return detail(orderId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> timeline(String status) {
        if ("CANCELLED".equals(status)) return List.of("PLACED", "CANCELLED");
        int idx = FLOW.indexOf(status);
        return idx < 0 ? List.of("PLACED") : FLOW.subList(0, idx + 1);
    }

    private String generateOrderNumber() {
        String day = DAY.format(java.time.LocalDate.now(ZoneOffset.UTC));
        String rand = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "KMR" + day + "-" + rand;
    }

    private String writeAddressJson(Address a) {
        try {
            return objectMapper.writeValueAsString(AddressService.toDto(a));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save address");
        }
    }

    private AddressDto readAddressJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, AddressDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}

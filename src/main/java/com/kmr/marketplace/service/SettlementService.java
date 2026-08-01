package com.kmr.marketplace.service;

import com.kmr.marketplace.entity.Order;
import com.kmr.marketplace.entity.OrderItem;
import com.kmr.marketplace.entity.Settlement;
import com.kmr.marketplace.entity.Shop;
import com.kmr.marketplace.repository.SettlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits an online-paid order: the delivery fee stays with the platform, and each
 * vendor's product amount is routed to their Razorpay linked account.
 *
 * Two modes (razorpay.split-mode):
 *   HOLD — create the vendor transfer "on hold" at payment; release it when the
 *          order is delivered (protects against cancellations/returns).
 *   SPOT — split immediately at payment.
 *
 * Without live Razorpay keys everything is simulated (records the intended split
 * with a dev transfer id), so the whole flow is testable now and flips to real
 * Route transfers once keys + linked accounts exist.
 */
@Service
@Transactional
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementRepository settlementRepo;
    private final PaymentService paymentService;

    public SettlementService(SettlementRepository settlementRepo, PaymentService paymentService) {
        this.settlementRepo = settlementRepo;
        this.paymentService = paymentService;
    }

    /** Called once an online order's payment is confirmed. Idempotent. */
    public void settleOrder(Order order) {
        if (settlementRepo.existsByOrderId(order.getId())) return;   // already settled

        boolean hold = paymentService.holdMode();
        String mode  = hold ? "HOLD" : "SPOT";
        String paymentId = order.getRazorpayPaymentId();

        // Sum each shop's product amount (delivery fee is the platform's, not split).
        Map<Long, ShopShare> byShop = new LinkedHashMap<>();
        for (OrderItem i : order.getItems()) {
            Shop shop = i.getShop();
            double line = i.getPrice().doubleValue() * i.getQuantity();
            byShop.computeIfAbsent(shop.getId(), k -> new ShopShare(shop))
                  .amount += line;
        }

        for (ShopShare s : byShop.values()) {
            Settlement st = new Settlement();
            st.setOrderId(order.getId());
            st.setShopId(s.shop.getId());
            st.setAmount(BigDecimal.valueOf(s.amount));
            st.setMode(mode);

            String accId = s.shop.getRazorpayAccountId();
            boolean canTransfer = paymentService.isLive() ? (accId != null && !accId.isBlank()) : true;

            if (paymentId != null && canTransfer) {
                String trf = paymentService.createTransfer(
                        paymentId, accId, st.getAmount(), hold, "order " + order.getOrderNumber());
                if (trf != null) {
                    st.setRazorpayTransferId(trf);
                    st.setStatus(hold ? "HELD" : "SETTLED");
                    if (!hold) st.setReleasedAt(Instant.now());
                } else {
                    st.setStatus("FAILED");
                }
            } else {
                // Vendor not onboarded to Route yet → record the owed amount for
                // manual settlement (or later auto-settle once they onboard).
                st.setStatus("PENDING");
            }
            settlementRepo.save(st);
        }
        log.info("Settled order {} in {} mode ({} shop(s))", order.getOrderNumber(), mode, byShop.size());
    }

    /** Release held vendor transfers when the order is delivered (HOLD mode). */
    public void releaseOrder(Order order) {
        for (Settlement st : settlementRepo.findByOrderId(order.getId())) {
            if (!"HELD".equals(st.getStatus())) continue;
            boolean ok = paymentService.releaseTransfer(st.getRazorpayTransferId());
            if (ok) {
                st.setStatus("RELEASED");
                st.setReleasedAt(Instant.now());
                settlementRepo.save(st);
            }
        }
    }

    /** On cancellation, void any not-yet-paid vendor transfers. */
    public void cancelOrder(Order order) {
        for (Settlement st : settlementRepo.findByOrderId(order.getId())) {
            if ("HELD".equals(st.getStatus()) || "PENDING".equals(st.getStatus())) {
                st.setStatus("CANCELLED");
                settlementRepo.save(st);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<com.kmr.marketplace.dto.SettlementDto> forOrder(Long orderId) {
        return settlementRepo.findByOrderId(orderId).stream().map(SettlementService::toDto).toList();
    }

    static com.kmr.marketplace.dto.SettlementDto toDto(Settlement s) {
        return new com.kmr.marketplace.dto.SettlementDto(
                s.getId(), s.getOrderId(), s.getShopId(), s.getAmount().doubleValue(),
                s.getMode(), s.getStatus(), s.getRazorpayTransferId(),
                s.getCreatedAt() != null ? s.getCreatedAt().toString() : null,
                s.getReleasedAt() != null ? s.getReleasedAt().toString() : null);
    }

    private static final class ShopShare {
        final Shop shop;
        double amount;
        ShopShare(Shop shop) { this.shop = shop; }
    }
}

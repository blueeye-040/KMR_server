package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A vendor's share of one order — the money that settles to that shop.
 * status: PENDING (not yet transferred) · HELD (transfer created, on hold) ·
 *         RELEASED (hold lifted) · SETTLED (spot-paid) · CANCELLED · FAILED.
 */
@Entity
@Table(name = "settlements")
public class Settlement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "shop_id")
    private Long shopId;

    private BigDecimal amount;
    private String mode;      // HOLD | SPOT
    private String status;

    @Column(name = "razorpay_transfer_id")
    private String razorpayTransferId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = "PENDING";
    }

    public Long getId()               { return id; }
    public Long getOrderId()          { return orderId; }
    public Long getShopId()           { return shopId; }
    public BigDecimal getAmount()     { return amount; }
    public String getMode()           { return mode; }
    public String getStatus()         { return status; }
    public String getRazorpayTransferId() { return razorpayTransferId; }
    public Instant getReleasedAt()    { return releasedAt; }
    public Instant getCreatedAt()     { return createdAt; }

    public void setOrderId(Long v)    { this.orderId = v; }
    public void setShopId(Long v)     { this.shopId = v; }
    public void setAmount(BigDecimal v){ this.amount = v; }
    public void setMode(String v)     { this.mode = v; }
    public void setStatus(String v)   { this.status = v; }
    public void setRazorpayTransferId(String v) { this.razorpayTransferId = v; }
    public void setReleasedAt(Instant v) { this.releasedAt = v; }
}

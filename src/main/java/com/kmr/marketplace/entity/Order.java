package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "delivery_charge")
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String status;          // PLACED | CONFIRMED | SHIPPED | OUT_FOR_DELIVERY | DELIVERED | CANCELLED

    @Column(name = "payment_method")
    private String paymentMethod;   // ONLINE | COD

    @Column(name = "payment_status")
    private String paymentStatus;   // PENDING | PAID | FAILED | REFUNDED

    // Snapshot of the delivery address at order time, stored as JSON.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", columnDefinition = "jsonb")
    private String shippingAddress;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public Long getId()                  { return id; }
    public String getOrderNumber()       { return orderNumber; }
    public User getUser()                { return user; }
    public BigDecimal getTotalAmount()   { return totalAmount; }
    public BigDecimal getDeliveryCharge(){ return deliveryCharge; }
    public BigDecimal getDiscountAmount(){ return discountAmount; }
    public String getStatus()            { return status; }
    public String getPaymentMethod()     { return paymentMethod; }
    public String getPaymentStatus()     { return paymentStatus; }
    public String getShippingAddress()   { return shippingAddress; }
    public String getRazorpayOrderId()   { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public List<OrderItem> getItems()    { return items; }
    public Instant getCreatedAt()        { return createdAt; }
    public Instant getUpdatedAt()        { return updatedAt; }

    public void setId(Long id)                       { this.id = id; }
    public void setOrderNumber(String v)             { this.orderNumber = v; }
    public void setUser(User user)                   { this.user = user; }
    public void setTotalAmount(BigDecimal v)         { this.totalAmount = v; }
    public void setDeliveryCharge(BigDecimal v)      { this.deliveryCharge = v; }
    public void setDiscountAmount(BigDecimal v)      { this.discountAmount = v; }
    public void setStatus(String status)             { this.status = status; }
    public void setPaymentMethod(String v)           { this.paymentMethod = v; }
    public void setPaymentStatus(String v)           { this.paymentStatus = v; }
    public void setShippingAddress(String v)         { this.shippingAddress = v; }
    public void setRazorpayOrderId(String v)         { this.razorpayOrderId = v; }
    public void setRazorpayPaymentId(String v)       { this.razorpayPaymentId = v; }
    public void setItems(List<OrderItem> items)      { this.items = items; }
    public void setCreatedAt(Instant v)              { this.createdAt = v; }
    public void setUpdatedAt(Instant v)              { this.updatedAt = v; }
}

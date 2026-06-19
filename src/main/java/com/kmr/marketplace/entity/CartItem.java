package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cart")
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_product_id", nullable = false)
    private ShopProduct shopProduct;

    private int quantity = 1;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public Long getId()                      { return id; }
    public User getUser()                    { return user; }
    public ShopProduct getShopProduct()      { return shopProduct; }
    public int getQuantity()                 { return quantity; }
    public Instant getCreatedAt()            { return createdAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setUser(User user)                    { this.user = user; }
    public void setShopProduct(ShopProduct sp)        { this.shopProduct = sp; }
    public void setQuantity(int quantity)             { this.quantity = quantity; }
    public void setCreatedAt(Instant createdAt)       { this.createdAt = createdAt; }
}

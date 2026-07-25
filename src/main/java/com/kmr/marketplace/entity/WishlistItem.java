package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wishlist")
public class WishlistItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId()           { return id; }
    public User getUser()         { return user; }
    public Product getProduct()   { return product; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id)              { this.id = id; }
    public void setUser(User user)          { this.user = user; }
    public void setProduct(Product product) { this.product = product; }
}

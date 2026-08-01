package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_product_id")
    private ShopProduct shopProduct;

    private short rating;
    private String title;
    private String body;

    @Column(name = "verified_purchase")
    private boolean verifiedPurchase;

    @Column(name = "helpful_count")
    private int helpfulCount;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public Long getId()                    { return id; }
    public Product getProduct()            { return product; }
    public User getUser()                  { return user; }
    public ShopProduct getShopProduct()    { return shopProduct; }
    public short getRating()               { return rating; }
    public String getTitle()               { return title; }
    public String getBody()                { return body; }
    public boolean isVerifiedPurchase()    { return verifiedPurchase; }
    public int getHelpfulCount()           { return helpfulCount; }
    public Instant getCreatedAt()          { return createdAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setProduct(Product product)           { this.product = product; }
    public void setUser(User user)                    { this.user = user; }
    public void setShopProduct(ShopProduct sp)        { this.shopProduct = sp; }
    public void setRating(short rating)               { this.rating = rating; }
    public void setTitle(String title)                { this.title = title; }
    public void setBody(String body)                  { this.body = body; }
    public void setVerifiedPurchase(boolean v)        { this.verifiedPurchase = v; }
    public void setHelpfulCount(int helpfulCount)     { this.helpfulCount = helpfulCount; }
    public void setCreatedAt(Instant createdAt)       { this.createdAt = createdAt; }
}

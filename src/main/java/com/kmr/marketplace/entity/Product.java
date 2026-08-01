package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String slug;              // stock keeping unit, unique identifier
    private String description;

    @Column(name = "image_url")
    private String imageUrl;     //column: image_url

    private boolean active;
    public boolean featured;

    @Column(name = "new_arrival")
    private boolean newArrival;   // column: new_arrival

    @Column(name = "top_selling")
    private boolean topSelling;   // column: top_selling
    
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
   
    private Instant createdAt;

    @Column(columnDefinition = "jsonb")
    private String specifications;

    @Column(name = "rating_avg")
    private java.math.BigDecimal ratingAvg;

    @Column(name = "review_count")
    private int reviewCount;



    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    // getters/setters — generate all for each field
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public boolean isFeatured() { return featured; }
    public boolean isNewArrival() { return newArrival; }
    public boolean isTopSelling() { return topSelling; }
    public Brand getBrand() { return brand; }
    public Category getCategory() { return category; }
    public Instant getCreatedAt() { return createdAt; }
    public String getSpecifications() { return specifications; }
    public java.math.BigDecimal getRatingAvg() { return ratingAvg; }
    public int getReviewCount() { return reviewCount; }

    // setters...
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setActive(boolean active) { this.active = active; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }
    public void setTopSelling(boolean topSelling) { this.topSelling = topSelling; }
    public void setBrand(Brand brand) { this.brand = brand; }
    public void setCategory(Category category) { this.category = category; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public void setRatingAvg(java.math.BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

}

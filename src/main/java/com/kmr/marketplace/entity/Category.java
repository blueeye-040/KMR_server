package com.kmr.marketplace.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String emoji;
    private String colorHex;       // electronics, fashion, etc.
    private String imageUrl;
    private boolean active;

    @Column(name = "parent_id")
    private Long parentId;          // null = department (top level); set = leaf category

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "created_at", updatable = false)
    private java.time.OffsetDateTime createdAt;

    // getters/setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public String getColorHex() { return colorHex; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public Long getParentId() { return parentId; }
    public int getSortOrder() { return sortOrder; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    @PrePersist
    void prePersist() { this.createdAt = OffsetDateTime.now(); }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setActive(boolean active) { this.active = active; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}

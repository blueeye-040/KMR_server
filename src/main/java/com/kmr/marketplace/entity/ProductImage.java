package com.kmr.marketplace.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order")
    private int sortOrder;

    public Long getId()            { return id; }
    public Product getProduct()    { return product; }
    public String getImageUrl()    { return imageUrl; }
    public int getSortOrder()      { return sortOrder; }

    public void setId(Long id)                  { this.id = id; }
    public void setProduct(Product product)     { this.product = product; }
    public void setImageUrl(String imageUrl)    { this.imageUrl = imageUrl; }
    public void setSortOrder(int sortOrder)     { this.sortOrder = sortOrder; }
}

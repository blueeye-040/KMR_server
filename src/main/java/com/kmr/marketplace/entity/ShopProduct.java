package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shop_products")
public class ShopProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private int discountPercent;
    private int stock;
    private int deliveryDays;
    private boolean available;

    public Long getId() { return id; }
    public Shop getShop() { return shop; }
    public Product getProduct() { return product; }
    public BigDecimal getMrp() { return mrp; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public int getDiscountPercent() { return discountPercent; }
    public int getStock() { return stock; }
    public int getDeliveryDays() { return deliveryDays; }
    public boolean isAvailable() { return available; }
    public void setId(Long id) { this.id = id; }
    public void setShop(Shop shop) { this.shop = shop; }
    public void setProduct(Product product) { this.product = product; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public void setDiscountPercent(int d) { this.discountPercent = d; }
    public void setStock(int stock) { this.stock = stock; }
    public void setDeliveryDays(int d) { this.deliveryDays = d; }
    public void setAvailable(boolean available) { this.available = available; }
}

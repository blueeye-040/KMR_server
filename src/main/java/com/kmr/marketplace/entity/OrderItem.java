package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_product_id")
    private ShopProduct shopProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // The vendor whose bank account receives this line's payment.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private int quantity;

    private BigDecimal price;    // unit selling price at order time

    private String status;       // per-line fulfilment status

    public Long getId()               { return id; }
    public Order getOrder()           { return order; }
    public ShopProduct getShopProduct(){ return shopProduct; }
    public Product getProduct()       { return product; }
    public Shop getShop()             { return shop; }
    public int getQuantity()          { return quantity; }
    public BigDecimal getPrice()      { return price; }
    public String getStatus()         { return status; }

    public void setId(Long id)                 { this.id = id; }
    public void setOrder(Order order)          { this.order = order; }
    public void setShopProduct(ShopProduct sp) { this.shopProduct = sp; }
    public void setProduct(Product product)    { this.product = product; }
    public void setShop(Shop shop)             { this.shop = shop; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
    public void setPrice(BigDecimal price)     { this.price = price; }
    public void setStatus(String status)       { this.status = status; }
}

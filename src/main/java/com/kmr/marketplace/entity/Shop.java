package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shops")
public class Shop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String ownerName;
    private String phone;
    private String email;
    private String address;
    private String city;
    
    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "upi_id")
    private String upiId;

    private String logoUrl;
    private String tagline;
    private BigDecimal rating;
    private long totalSales;
    private boolean approved;

    @Column(name = "is_official")
    private boolean isOfficial;

    @Column(name = "created_at", updatable = false)
    private java.time.OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getOwnerName() { return ownerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getBankAccount() { return bankAccount; }
    public String getUpiId() { return upiId; }
    public String getLogoUrl() { return logoUrl; }
    public String getTagline() { return tagline; }
    public BigDecimal getRating() { return rating; }
    public long getTotalSales() { return totalSales; }
    public boolean isApproved() { return approved; }
    public boolean isOfficial() { return isOfficial; }

    @PrePersist
    void prePersist() { this.createdAt = java.time.OffsetDateTime.now(); }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setTotalSales(long totalSales) { this.totalSales = totalSales; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setIsOfficial(boolean isOfficial) { this.isOfficial = isOfficial; }
}

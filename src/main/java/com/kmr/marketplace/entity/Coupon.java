package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String description;
    private String type;              // FLAT | PERCENT

    private BigDecimal value;

    @Column(name = "min_cart_value")
    private BigDecimal minCartValue;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    private boolean active;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private int usedCount;

    public Long getId()             { return id; }
    public String getCode()         { return code; }
    public String getDescription()  { return description; }
    public String getType()         { return type; }
    public BigDecimal getValue()    { return value; }
    public BigDecimal getMinCartValue() { return minCartValue; }
    public BigDecimal getMaxDiscount()  { return maxDiscount; }
    public boolean isActive()       { return active; }
    public Instant getExpiresAt()   { return expiresAt; }
    public Integer getUsageLimit()  { return usageLimit; }
    public int getUsedCount()       { return usedCount; }

    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public void setCode(String code)               { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type)               { this.type = type; }
    public void setValue(BigDecimal value)         { this.value = value; }
    public void setMinCartValue(BigDecimal v)      { this.minCartValue = v; }
    public void setMaxDiscount(BigDecimal v)       { this.maxDiscount = v; }
    public void setActive(boolean active)          { this.active = active; }
    public void setExpiresAt(Instant expiresAt)    { this.expiresAt = expiresAt; }
    public void setUsageLimit(Integer usageLimit)  { this.usageLimit = usageLimit; }
}

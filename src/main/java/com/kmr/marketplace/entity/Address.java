package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "addresses")
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String phone;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;
    private String pincode;

    @Column(name = "is_default")
    private boolean isDefault;

    private String type;   // HOME | WORK | OTHER

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId()             { return id; }
    public User getUser()           { return user; }
    public String getName()         { return name; }
    public String getPhone()        { return phone; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity()         { return city; }
    public String getState()        { return state; }
    public String getPincode()      { return pincode; }
    public boolean isDefault()      { return isDefault; }
    public String getType()         { return type; }
    public Instant getCreatedAt()   { return createdAt; }

    public void setId(Long id)                    { this.id = id; }
    public void setUser(User user)                { this.user = user; }
    public void setName(String name)              { this.name = name; }
    public void setPhone(String phone)            { this.phone = phone; }
    public void setAddressLine1(String v)         { this.addressLine1 = v; }
    public void setAddressLine2(String v)         { this.addressLine2 = v; }
    public void setCity(String city)              { this.city = city; }
    public void setState(String state)            { this.state = state; }
    public void setPincode(String pincode)        { this.pincode = pincode; }
    public void setDefault(boolean isDefault)     { this.isDefault = isDefault; }
    public void setType(String type)              { this.type = type; }
    public void setCreatedAt(Instant createdAt)   { this.createdAt = createdAt; }
}

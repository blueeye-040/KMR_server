package com.kmr.marketplace.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String logoUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLogoUrl() { return logoUrl; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}

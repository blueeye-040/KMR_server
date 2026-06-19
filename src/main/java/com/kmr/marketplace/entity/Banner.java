package com.kmr.marketplace.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "bg_color_hex")
    private String bgColorHex;

    @Column(name = "sort_order")
    private int sortOrder;

    private boolean active;

    public Long getId()             { return id; }
    public String getTitle()        { return title; }
    public String getSubtitle()     { return subtitle; }
    public String getImageUrl()     { return imageUrl; }
    public String getBgColorHex()   { return bgColorHex; }
    public int getSortOrder()       { return sortOrder; }
    public boolean isActive()       { return active; }

    public void setId(Long id)                    { this.id = id; }
    public void setTitle(String title)            { this.title = title; }
    public void setSubtitle(String subtitle)      { this.subtitle = subtitle; }
    public void setImageUrl(String imageUrl)      { this.imageUrl = imageUrl; }
    public void setBgColorHex(String bgColorHex)  { this.bgColorHex = bgColorHex; }
    public void setSortOrder(int sortOrder)       { this.sortOrder = sortOrder; }
    public void setActive(boolean active)         { this.active = active; }
}

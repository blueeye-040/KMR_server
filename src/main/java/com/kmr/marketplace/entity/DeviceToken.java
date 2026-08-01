package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;
    private String platform;   // ANDROID | IOS

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId()       { return id; }
    public User getUser()     { return user; }
    public String getToken()  { return token; }
    public String getPlatform(){ return platform; }

    public void setUser(User user)       { this.user = user; }
    public void setToken(String token)   { this.token = token; }
    public void setPlatform(String p)    { this.platform = p; }
}

package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public User() {}

    private User(Builder b) {
        this.name      = b.name;
        this.email     = b.email;
        this.phone     = b.phone;
        this.password  = b.password;
        this.role      = b.role != null ? b.role : UserRole.CUSTOMER;
        this.avatarUrl = b.avatarUrl;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name, email, phone, password, avatarUrl;
        private UserRole role;
        public Builder name(String v)      { name = v;      return this; }
        public Builder email(String v)     { email = v;     return this; }
        public Builder phone(String v)     { phone = v;     return this; }
        public Builder password(String v)  { password = v;  return this; }
        public Builder role(UserRole v)    { role = v;      return this; }
        public Builder avatarUrl(String v) { avatarUrl = v; return this; }
        public User build()                { return new User(this); }
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId()                    { return id; }
    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }
    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }
    public String getPhone()              { return phone; }
    public void setPhone(String phone)    { this.phone = phone; }
    public UserRole getRole()             { return role; }
    public void setRole(UserRole role)    { this.role = role; }
    public String getAvatarUrl()          { return avatarUrl; }
    public void setAvatarUrl(String u)    { this.avatarUrl = u; }
    public OffsetDateTime getCreatedAt()  { return createdAt; }

    // ── UserDetails ───────────────────────────────────────────
    @Override public String getPassword() { return password; }
    public void setPassword(String p)     { this.password = p; }

    @Override
    public String getUsername() { return email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

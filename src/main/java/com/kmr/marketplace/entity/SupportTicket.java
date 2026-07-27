package com.kmr.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_id")
    private Long orderId;

    private String type;      // ISSUE | RETURN | EXCHANGE
    private String subject;
    private String message;
    private String status;    // OPEN | IN_PROGRESS | RESOLVED

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = "OPEN";
    }

    public Long getId()          { return id; }
    public User getUser()        { return user; }
    public Long getOrderId()     { return orderId; }
    public String getType()      { return type; }
    public String getSubject()   { return subject; }
    public String getMessage()   { return message; }
    public String getStatus()    { return status; }
    public Instant getCreatedAt(){ return createdAt; }

    public void setUser(User user)        { this.user = user; }
    public void setOrderId(Long orderId)  { this.orderId = orderId; }
    public void setType(String type)      { this.type = type; }
    public void setSubject(String s)      { this.subject = s; }
    public void setMessage(String m)      { this.message = m; }
    public void setStatus(String status)  { this.status = status; }
}

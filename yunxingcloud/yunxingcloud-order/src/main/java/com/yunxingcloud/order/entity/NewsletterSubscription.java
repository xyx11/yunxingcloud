package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_subscription")
public class NewsletterSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(length = 1) private String status = "1"; // 1=active, 0=unsubscribed
    @Column(name = "subscribed_at") private LocalDateTime subscribedAt;
    @Column(name = "unsubscribed_at") private LocalDateTime unsubscribedAt;

    @PrePersist void onCreate() { if (subscribedAt == null) subscribedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; } public void setSubscribedAt(LocalDateTime v) { subscribedAt = v; }
    public LocalDateTime getUnsubscribedAt() { return unsubscribedAt; } public void setUnsubscribedAt(LocalDateTime v) { unsubscribedAt = v; }
}

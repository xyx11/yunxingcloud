package com.yunxingcloud.yunxingcloud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    private static final int EXPIRY_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = LocalDateTime.now().plusHours(EXPIRY_HOURS);
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}

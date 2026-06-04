package com.yunxingcloud.usercenter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "providerUserId"})
})
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 200)
    private String providerUserId;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 500)
    private String accessToken;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public SocialAccount() {}

    public SocialAccount(Long userId, String provider, String providerUserId) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

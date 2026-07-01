package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 降价提醒 */
@Entity @Table(name = "price_alert")
public class PriceAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "target_price", nullable = false) private Long targetPrice; // 期望价格(分)
    @Column(name = "notified") private Boolean notified = false;  // 是否已通知
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Long getTargetPrice() { return targetPrice; } public void setTargetPrice(Long v) { targetPrice = v; }
    public Boolean getNotified() { return notified; } public void setNotified(Boolean v) { notified = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
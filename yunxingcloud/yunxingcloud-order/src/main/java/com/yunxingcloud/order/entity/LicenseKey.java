package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 激活码/License Key */
@Entity @Table(name = "license_key")
public class LicenseKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "order_id") private Long orderId;
    @Column(name = "key_code", length = 64, unique = true, nullable = false) private String keyCode;
    @Column(length = 1) private String status = "0"; // 0未使用/1已激活/2已过期
    @Column(name = "activated_by", length = 100) private String activatedBy;
    @Column(name = "activate_at") private LocalDateTime activateAt;
    @Column(name = "expire_at") private LocalDateTime expireAt;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getKeyCode() { return keyCode; } public void setKeyCode(String v) { keyCode = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getActivatedBy() { return activatedBy; } public void setActivatedBy(String v) { activatedBy = v; }
    public LocalDateTime getActivateAt() { return activateAt; } public void setActivateAt(LocalDateTime v) { activateAt = v; }
    public LocalDateTime getExpireAt() { return expireAt; } public void setExpireAt(LocalDateTime v) { expireAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 组合商品 (套餐) */
@Entity @Table(name = "product_bundle")
public class ProductBundle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String name;
    @Column(name = "bundle_price", nullable = false) private Long bundlePrice; // 套餐价(分)
    @Column(name = "original_price") private Long originalPrice; // 原价总和
    @Column(length = 1000) private String productIds;  // 包含商品ID(逗号分隔)
    @Column(length = 500) private String imageUrl;
    @Column(length = 1) private String status = "1";
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public Long getBundlePrice() { return bundlePrice; } public void setBundlePrice(Long v) { bundlePrice = v; }
    public Long getOriginalPrice() { return originalPrice; } public void setOriginalPrice(Long v) { originalPrice = v; }
    public String getProductIds() { return productIds; } public void setProductIds(String v) { productIds = v; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { imageUrl = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
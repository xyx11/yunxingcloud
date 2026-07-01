package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 价格变更历史 */
@Entity @Table(name = "price_history")
public class PriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "old_price", nullable = false) private Long oldPrice;
    @Column(name = "new_price", nullable = false) private Long newPrice;
    @Column(name = "changed_by", length = 100) private String changedBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Long getOldPrice() { return oldPrice; } public void setOldPrice(Long v) { oldPrice = v; }
    public Long getNewPrice() { return newPrice; } public void setNewPrice(Long v) { newPrice = v; }
    public String getChangedBy() { return changedBy; } public void setChangedBy(String v) { changedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
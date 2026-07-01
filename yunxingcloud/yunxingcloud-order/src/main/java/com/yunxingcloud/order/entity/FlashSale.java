package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 秒杀活动 */
@Entity @Table(name = "flash_sale")
public class FlashSale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "flash_price", nullable = false) private Long flashPrice;   // 秒杀价(分)
    @Column(name = "stock", nullable = false) private Integer stock;            // 秒杀库存
    @Column(name = "sold") private Integer sold = 0;                            // 已售
    @Column(name = "limit_per_user") private Integer limitPerUser = 1;          // 每人限购
    @Column(name = "start_time", nullable = false) private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false) private LocalDateTime endTime;
    @Column(length = 1) private String status = "0"; // 0未开始/1进行中/2已结束
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Long getFlashPrice() { return flashPrice; } public void setFlashPrice(Long v) { flashPrice = v; }
    public Integer getStock() { return stock; } public void setStock(Integer v) { stock = v; }
    public Integer getSold() { return sold; } public void setSold(Integer v) { sold = v; }
    public Integer getLimitPerUser() { return limitPerUser; } public void setLimitPerUser(Integer v) { limitPerUser = v; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime v) { startTime = v; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime v) { endTime = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
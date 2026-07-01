package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 积分流水 */
@Entity @Table(name = "points_record")
public class PointsRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(nullable = false) private Long amount;           // 变动量(正=获得,负=消费)
    @Column(length = 20, nullable = false) private String type; // earn/purchase/redeem/expire/refund
    @Column(name = "order_id") private Long orderId;
    @Column(length = 500) private String remark;
    @Column(name = "balance_after") private Long balanceAfter; // 变动后余额
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public Long getAmount() { return amount; } public void setAmount(Long v) { amount = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { remark = v; }
    public Long getBalanceAfter() { return balanceAfter; } public void setBalanceAfter(Long v) { balanceAfter = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
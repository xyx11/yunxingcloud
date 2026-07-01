package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "coupon_user")
public class CouponUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "coupon_id", nullable = false) private Long couponId;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 1) private String status = "0";
    @Column(name = "used_order_id") private Long usedOrderId;
    @Column(name = "used_at") private LocalDateTime usedAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public CouponUser() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCouponId() { return couponId; } public void setCouponId(Long c) { this.couponId = c; }
    public String getUsername() { return username; } public void setUsername(String u) { this.username = u; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Long getUsedOrderId() { return usedOrderId; } public void setUsedOrderId(Long o) { this.usedOrderId = o; }
    public LocalDateTime getUsedAt() { return usedAt; } public void setUsedAt(LocalDateTime u) { this.usedAt = u; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

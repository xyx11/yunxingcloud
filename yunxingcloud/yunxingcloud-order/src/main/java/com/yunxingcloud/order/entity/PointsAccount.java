package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 积分账户 */
@Entity @Table(name = "points_account")
public class PointsAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100, unique = true) private String username;
    @Column(nullable = false) private Long balance = 0L;       // 可用积分
    @Column(name = "total_earned") private Long totalEarned = 0L; // 累计获得
    @Column(name = "total_spent") private Long totalSpent = 0L;   // 累计使用
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public Long getBalance() { return balance; } public void setBalance(Long v) { balance = v; }
    public Long getTotalEarned() { return totalEarned; } public void setTotalEarned(Long v) { totalEarned = v; }
    public Long getTotalSpent() { return totalSpent; } public void setTotalSpent(Long v) { totalSpent = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 拼团记录: 某用户的参团记录 */
@Entity @Table(name = "group_record")
public class GroupRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "group_id", nullable = false) private Long groupId;       // 团ID (订单号)
    @Column(name = "group_buy_id", nullable = false) private Long groupBuyId; // 拼团活动ID
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(nullable = false, length = 100) private String username;
    @Column(name = "is_leader") private Boolean isLeader = false;  // 团长
    @Column(length = 1) private String status = "0"; // 0拼团中/1成团/2失败
    @Column(name = "joined_at") private LocalDateTime joinedAt;
    @PrePersist void onCreate() { if (joinedAt == null) joinedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; } public void setGroupId(Long v) { groupId = v; }
    public Long getGroupBuyId() { return groupBuyId; } public void setGroupBuyId(Long v) { groupBuyId = v; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public Boolean getIsLeader() { return isLeader; } public void setIsLeader(Boolean v) { isLeader = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getJoinedAt() { return joinedAt; } public void setJoinedAt(LocalDateTime v) { joinedAt = v; }
}
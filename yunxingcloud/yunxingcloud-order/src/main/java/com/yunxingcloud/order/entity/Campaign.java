package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 营销活动 (满减/折扣/赠品) */
@Entity @Table(name = "campaign")
public class Campaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 20, nullable = false) private String type;     // full_reduction/discount/gift
    @Column(name = "rule_json", columnDefinition = "TEXT") private String ruleJson; // 规则JSON
    @Column(name = "threshold") private Long threshold;  // 满减门槛(分)
    @Column(name = "discount") private Long discount;    // 满减金额(分) 或 折扣率(百分比)
    @Column(name = "max_discount") private Long maxDiscount; // 最大优惠(分)
    @Column(name = "gift_product_id") private Long giftProductId; // 赠品ID
    @Column(name = "total_stock") private Integer totalStock;     // 总库存
    @Column(name = "used_count") private Integer usedCount = 0;   // 已使用
    @Column(name = "limit_per_user") private Integer limitPerUser = 1;
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;
    @Column(length = 1) private String status = "0"; // 0未开始/1进行中/2已结束
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public String getRuleJson() { return ruleJson; } public void setRuleJson(String v) { ruleJson = v; }
    public Long getThreshold() { return threshold; } public void setThreshold(Long v) { threshold = v; }
    public Long getDiscount() { return discount; } public void setDiscount(Long v) { discount = v; }
    public Long getMaxDiscount() { return maxDiscount; } public void setMaxDiscount(Long v) { maxDiscount = v; }
    public Long getGiftProductId() { return giftProductId; } public void setGiftProductId(Long v) { giftProductId = v; }
    public Integer getTotalStock() { return totalStock; } public void setTotalStock(Integer v) { totalStock = v; }
    public Integer getUsedCount() { return usedCount; } public void setUsedCount(Integer v) { usedCount = v; }
    public Integer getLimitPerUser() { return limitPerUser; } public void setLimitPerUser(Integer v) { limitPerUser = v; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime v) { startTime = v; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime v) { endTime = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
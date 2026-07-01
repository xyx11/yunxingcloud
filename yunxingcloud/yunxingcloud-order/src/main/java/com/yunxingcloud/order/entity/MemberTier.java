package com.yunxingcloud.order.entity;

import jakarta.persistence.*;

/** 会员等级配置 */
@Entity @Table(name = "member_tier")
public class MemberTier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 20) private String name;     // VIP1/VIP2/VIP3
    @Column(name = "min_points", nullable = false) private Long minPoints;  // 升级所需积分
    @Column(name = "discount_rate") private Integer discountRate = 100;     // 折扣率(100=原价, 95=9.5折)
    @Column(name = "free_shipping") private Boolean freeShipping = false;   // 包邮
    @Column(name = "birthday_gift") private Boolean birthdayGift = false;   // 生日礼
    @Column(name = "priority_support") private Boolean prioritySupport = false; // 优先客服
    @Column private Integer sortOrder = 0;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public Long getMinPoints() { return minPoints; } public void setMinPoints(Long v) { minPoints = v; }
    public Integer getDiscountRate() { return discountRate; } public void setDiscountRate(Integer v) { discountRate = v; }
    public Boolean getFreeShipping() { return freeShipping; } public void setFreeShipping(Boolean v) { freeShipping = v; }
    public Boolean getBirthdayGift() { return birthdayGift; } public void setBirthdayGift(Boolean v) { birthdayGift = v; }
    public Boolean getPrioritySupport() { return prioritySupport; } public void setPrioritySupport(Boolean v) { prioritySupport = v; }
    public Integer getSortOrder() { return sortOrder; } public void setSortOrder(Integer v) { sortOrder = v; }
}
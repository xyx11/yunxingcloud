package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 礼品卡/储值卡 */
@Entity @Table(name = "gift_card")
public class GiftCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "card_no", length = 30, unique = true, nullable = false) private String cardNo;
    @Column(nullable = false) private Long amount;          // 面额(分)
    @Column(nullable = false) private Long balance;         // 余额(分)
    @Column(length = 1) private String status = "0";        // 0未激活/1已激活/2已用完/3已过期
    @Column(name = "activate_at") private LocalDateTime activateAt;
    @Column(name = "expire_at") private LocalDateTime expireAt;
    @Column(name = "owner", length = 100) private String owner; // 激活用户
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCardNo() { return cardNo; } public void setCardNo(String v) { cardNo = v; }
    public Long getAmount() { return amount; } public void setAmount(Long v) { amount = v; }
    public Long getBalance() { return balance; } public void setBalance(Long v) { balance = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getActivateAt() { return activateAt; } public void setActivateAt(LocalDateTime v) { activateAt = v; }
    public LocalDateTime getExpireAt() { return expireAt; } public void setExpireAt(LocalDateTime v) { expireAt = v; }
    public String getOwner() { return owner; } public void setOwner(String v) { owner = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
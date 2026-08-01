package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_card_transaction")
public class GiftCardTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "card_id", nullable = false) private Long cardId;
    @Column(name = "card_no", length = 32) private String cardNo;
    @Column(length = 20, nullable = false) private String type; // ACTIVATE/PURCHASE/PAY/REFUND
    @Column(nullable = false) private Long amount;
    @Column(length = 500) private String remark;
    @Column(name = "created_at") private LocalDateTime createdAt;

    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCardId() { return cardId; } public void setCardId(Long v) { cardId = v; }
    public String getCardNo() { return cardNo; } public void setCardNo(String v) { cardNo = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public Long getAmount() { return amount; } public void setAmount(Long v) { amount = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { remark = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}

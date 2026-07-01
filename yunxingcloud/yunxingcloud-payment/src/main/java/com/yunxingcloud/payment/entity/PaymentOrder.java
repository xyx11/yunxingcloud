package com.yunxingcloud.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", length = 30, unique = true, nullable = false)
    private String orderNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 20, nullable = false)
    private String channel;

    @Column(length = 1)
    private String status = "0";

    @Column(name = "trade_no", length = 100)
    private String tradeNo;

    @Column(length = 100)
    private String buyer;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refund_amount")
    private Long refundAmount = 0L;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PaymentOrder() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public Long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Long refundAmount) { this.refundAmount = refundAmount; }
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    public LocalDateTime getRefundAt() { return refundAt; }
    public void setRefundAt(LocalDateTime refundAt) { this.refundAt = refundAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

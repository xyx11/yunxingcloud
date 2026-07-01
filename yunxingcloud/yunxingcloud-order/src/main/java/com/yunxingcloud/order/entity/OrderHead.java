package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_head")
public class OrderHead {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", length = 30, unique = true, nullable = false)
    private String orderNo;
    @Column(nullable = false, length = 100)
    private String username;
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;
    @Column(name = "coupon_id")
    private Long couponId;
    @Column(name = "coupon_amount")
    private Long couponAmount = 0L;
    @Column(name = "actual_amount")
    private Long actualAmount;
    @Column(length = 1)
    private String status = "0";
    @Column(name = "payment_order_id")
    private Long paymentOrderId;
    @Column(name = "receiver_name", length = 50)
    private String receiverName;
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;
    @Column(name = "receiver_address", length = 500)
    private String receiverAddress;
    @Column(length = 500)
    private String remark;
    @Column(name = "expire_at")
    private LocalDateTime expireAt;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderHead() {}
    @PrePersist protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (expireAt == null) expireAt = createdAt.plusMinutes(15);
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Long getCouponAmount() { return couponAmount; }
    public void setCouponAmount(Long couponAmount) { this.couponAmount = couponAmount; }
    public Long getActualAmount() { return actualAmount; }
    public void setActualAmount(Long actualAmount) { this.actualAmount = actualAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPaymentOrderId() { return paymentOrderId; }
    public void setPaymentOrderId(Long paymentOrderId) { this.paymentOrderId = paymentOrderId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
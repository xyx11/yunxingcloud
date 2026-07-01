package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 售后单: 退货/退款/换货 */
@Entity @Table(name = "after_sale")
public class AfterSale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "order_no", length = 30) private String orderNo;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 20, nullable = false) private String type;          // refund/return/exchange
    @Column(length = 500) private String reason;
    @Column(name = "refund_amount") private Long refundAmount;           // 退款金额(分)
    @Column(name = "evidence_urls", length = 2000) private String evidenceUrls; // 凭证图片
    @Column(length = 1) private String status = "0";  // 0待审核/1通过/2拒绝/3退款中/4完成
    @Column(length = 500) private String remark;      // 审核备注
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String v) { orderNo = v; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public Long getRefundAmount() { return refundAmount; } public void setRefundAmount(Long v) { refundAmount = v; }
    public String getEvidenceUrls() { return evidenceUrls; } public void setEvidenceUrls(String v) { evidenceUrls = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { remark = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
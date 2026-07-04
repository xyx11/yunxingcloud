package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 预售活动 */
@Entity @Table(name = "presale")
public class Presale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "product_name", length = 200) private String productName;
    @Column(name = "deposit_price", nullable = false) private Long depositPrice;   // 定金价(分)
    @Column(name = "full_price", nullable = false) private Long fullPrice;          // 全价(分)
    @Column(name = "min_deposit", nullable = false) private Long minDeposit;        // 最低定金(分)
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;                       // 定金阶段结束
    @Column(name = "final_pay_start") private LocalDateTime finalPayStart;          // 尾款开始时间
    @Column(name = "final_pay_end") private LocalDateTime finalPayEnd;
    @Column(length = 1) private String status = "0"; // 0-预告/1-定金阶段/2-尾款阶段/3-结束
    @Column private Integer stock = 0;
    @Column private Integer sold = 0;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public String getProductName() { return productName; } public void setProductName(String v) { productName = v; }
    public Long getDepositPrice() { return depositPrice; } public void setDepositPrice(Long v) { depositPrice = v; }
    public Long getFullPrice() { return fullPrice; } public void setFullPrice(Long v) { fullPrice = v; }
    public Long getMinDeposit() { return minDeposit; } public void setMinDeposit(Long v) { minDeposit = v; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime v) { startTime = v; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime v) { endTime = v; }
    public LocalDateTime getFinalPayStart() { return finalPayStart; } public void setFinalPayStart(LocalDateTime v) { finalPayStart = v; }
    public LocalDateTime getFinalPayEnd() { return finalPayEnd; } public void setFinalPayEnd(LocalDateTime v) { finalPayEnd = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public Integer getStock() { return stock; } public void setStock(Integer v) { stock = v; }
    public Integer getSold() { return sold; } public void setSold(Integer v) { sold = v; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { imageUrl = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}

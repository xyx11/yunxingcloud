package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 库存盘点记录 */
@Entity @Table(name = "stock_take")
public class StockTake {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "system_qty") private Integer systemQty;    // 系统库存
    @Column(name = "actual_qty") private Integer actualQty;    // 实际盘点
    @Column(name = "difference") private Integer difference;   // 差异
    @Column(length = 500) private String remark;
    @Column(length = 100) private String operator;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Integer getSystemQty() { return systemQty; } public void setSystemQty(Integer v) { systemQty = v; }
    public Integer getActualQty() { return actualQty; } public void setActualQty(Integer v) { actualQty = v; }
    public Integer getDifference() { return difference; } public void setDifference(Integer v) { difference = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { remark = v; }
    public String getOperator() { return operator; } public void setOperator(String v) { operator = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
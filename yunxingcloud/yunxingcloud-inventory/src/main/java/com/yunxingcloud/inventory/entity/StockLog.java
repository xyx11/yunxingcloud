package com.yunxingcloud.inventory.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "stock_log")
public class StockLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(length = 20, nullable = false) private String type;
    @Column(nullable = false) private Integer quantity;
    @Column(name = "order_id") private Long orderId;
    @Column(length = 500) private String remark;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public StockLog() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId = p; }
    public Long getWarehouseId() { return warehouseId; } public void setWarehouseId(Long w) { this.warehouseId = w; }
    public String getType() { return type; } public void setType(String t) { this.type = t; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer q) { this.quantity = q; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long o) { this.orderId = o; }
    public String getRemark() { return remark; } public void setRemark(String r) { this.remark = r; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

package com.yunxingcloud.inventory.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "stock")
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "product_name", length = 200) private String productName;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(nullable = false) private Integer quantity = 0;
    @Column(name = "reserved") private Integer reserved = 0;
    @Column(name = "min_quantity") private Integer minQuantity = 10;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    public Stock() {}
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long pid) { this.productId = pid; }
    public String getProductName() { return productName; } public void setProductName(String n) { this.productName = n; }
    public Long getWarehouseId() { return warehouseId; } public void setWarehouseId(Long wid) { this.warehouseId = wid; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer q) { this.quantity = q; }
    public Integer getReserved() { return reserved; } public void setReserved(Integer r) { this.reserved = r; }
    public Integer getMinQuantity() { return minQuantity; } public void setMinQuantity(Integer m) { this.minQuantity = m; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}

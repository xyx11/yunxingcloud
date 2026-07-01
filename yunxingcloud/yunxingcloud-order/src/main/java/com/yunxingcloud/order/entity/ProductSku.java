package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "product_sku")
public class ProductSku {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(length = 200) private String name;
    @Column(nullable = false) private Long price;
    @Column private Integer stock = 0;
    @Column(name = "sku_code", length = 100) private String skuCode;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public ProductSku() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId = p; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public Long getPrice() { return price; } public void setPrice(Long p) { this.price = p; }
    public Integer getStock() { return stock; } public void setStock(Integer s) { this.stock = s; }
    public String getSkuCode() { return skuCode; } public void setSkuCode(String s) { this.skuCode = s; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

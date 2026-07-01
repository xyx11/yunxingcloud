package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_line")
public class OrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "product_name", length = 200)
    private String productName;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private Integer quantity = 1;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public OrderLine() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

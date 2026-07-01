package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "product_review")
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(length = 100) private String username;
    @Column(nullable = false) private Integer rating;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(columnDefinition = "TEXT") private String images;
    @Column(name = "order_id") private Long orderId;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public ProductReview() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId = p; }
    public String getUsername() { return username; } public void setUsername(String u) { this.username = u; }
    public Integer getRating() { return rating; } public void setRating(Integer r) { this.rating = r; }
    public String getContent() { return content; } public void setContent(String c) { this.content = c; }
    public String getImages() { return images; } public void setImages(String i) { this.images = i; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long o) { this.orderId = o; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

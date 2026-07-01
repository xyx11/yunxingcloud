package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "product_brand")
public class ProductBrand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 500) private String logo;
    @Column(length = 500) private String description;
    @Column(length = 1) private String status = "0";
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public ProductBrand() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public String getLogo() { return logo; } public void setLogo(String l) { this.logo = l; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

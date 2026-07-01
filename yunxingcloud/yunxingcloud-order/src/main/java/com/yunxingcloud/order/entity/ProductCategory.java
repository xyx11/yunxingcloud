package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "product_category")
public class ProductCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "parent_id") private Long parentId = 0L;
    @Column(name = "sort_order") private Integer sortOrder = 0;
    @Column(length = 200) private String icon;
    @Column(length = 1) private String status = "0";
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public ProductCategory() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public Long getParentId() { return parentId; } public void setParentId(Long p) { this.parentId = p; }
    public Integer getSortOrder() { return sortOrder; } public void setSortOrder(Integer s) { this.sortOrder = s; }
    public String getIcon() { return icon; } public void setIcon(String i) { this.icon = i; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private Long price;
    @Column private Integer stock = 0;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "brand_id")
    private Long brandId;
    @Column(columnDefinition = "TEXT")
    private String images;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column private Integer sales = 0;
    @Column(name = "is_new")
    private Boolean isNew = false;
    @Column(name = "is_hot")
    private Boolean isHot = false;
    @Column(length = 500)
    private String tags;
    @Column(name = "meta_title", length = 200)
    private String metaTitle;
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    @Column(length = 1)
    private String status = "0";
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Product() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getMetaTitle() { return metaTitle; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

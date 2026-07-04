package com.yunxingcloud.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductDTO {

    @NotBlank @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull @Positive
    private Long price;

    private Integer stock = 0;
    private Long categoryId;
    private Long brandId;
    private String images;
    private String imageUrl;
    private Boolean isNew = false;
    private Boolean isHot = false;
    private String tags;
    private String status;

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
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

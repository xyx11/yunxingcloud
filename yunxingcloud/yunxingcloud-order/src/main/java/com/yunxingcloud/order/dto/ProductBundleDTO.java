package com.yunxingcloud.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductBundleDTO {

    @NotBlank @Size(max = 200)
    private String name;

    @NotNull @Positive
    private Long bundlePrice;

    private Long originalPrice;
    private String productIds;
    private String imageUrl;
    private String status = "1";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getBundlePrice() { return bundlePrice; }
    public void setBundlePrice(Long bundlePrice) { this.bundlePrice = bundlePrice; }
    public Long getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Long originalPrice) { this.originalPrice = originalPrice; }
    public String getProductIds() { return productIds; }
    public void setProductIds(String productIds) { this.productIds = productIds; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

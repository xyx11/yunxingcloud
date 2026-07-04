package com.yunxingcloud.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductSkuDTO {

    @NotBlank @Size(max = 200)
    private String name;

    @NotNull @Positive
    private Long price;

    private Integer stock = 0;
    private String skuCode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
}

package com.yunxingcloud.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BrandDTO {
    @NotBlank @Size(max = 100)
    private String name;

    private String logo;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

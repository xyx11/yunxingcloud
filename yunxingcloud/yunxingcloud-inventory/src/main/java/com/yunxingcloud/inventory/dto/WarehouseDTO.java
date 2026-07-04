package com.yunxingcloud.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WarehouseDTO {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 300)
    private String address;
    private String status = "0";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 数字商品 (软件/课程/激活码) */
@Entity @Table(name = "digital_product")
public class DigitalProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "file_url", length = 500) private String fileUrl;     // 下载地址
    @Column(name = "file_size") private Long fileSize;                    // 文件大小(bytes)
    @Column(name = "auto_deliver") private Boolean autoDeliver = true;    // 自动发货
    @Column(name = "total_keys") private Integer totalKeys;              // 总激活码数
    @Column(name = "used_keys") private Integer usedKeys = 0;            // 已使用
    @Column(length = 1) private String status = "1";
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public String getFileUrl() { return fileUrl; } public void setFileUrl(String v) { fileUrl = v; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long v) { fileSize = v; }
    public Boolean getAutoDeliver() { return autoDeliver; } public void setAutoDeliver(Boolean v) { autoDeliver = v; }
    public Integer getTotalKeys() { return totalKeys; } public void setTotalKeys(Integer v) { totalKeys = v; }
    public Integer getUsedKeys() { return usedKeys; } public void setUsedKeys(Integer v) { usedKeys = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
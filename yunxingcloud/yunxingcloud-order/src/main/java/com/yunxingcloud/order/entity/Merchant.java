package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 商家入驻 */
@Entity @Table(name = "merchant")
public class Merchant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shop_name", nullable = false, length = 200) private String shopName;
    @Column(name = "owner_name", nullable = false, length = 100) private String ownerName;
    @Column(nullable = false, length = 20) private String phone;
    @Column(length = 200) private String email;
    @Column(name = "id_card", length = 50) private String idCard;
    @Column(name = "business_license", length = 500) private String businessLicense;
    @Column(length = 1) private String status = "0"; // 0-待审核/1-已通过/2-已拒绝
    @Column(length = 1000) private String description;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getShopName() { return shopName; } public void setShopName(String v) { shopName = v; }
    public String getOwnerName() { return ownerName; } public void setOwnerName(String v) { ownerName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { idCard = v; }
    public String getBusinessLicense() { return businessLicense; } public void setBusinessLicense(String v) { businessLicense = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}

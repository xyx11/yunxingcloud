package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "user_address")
public class UserAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 50, nullable = false) private String name;
    @Column(length = 20, nullable = false) private String phone;
    @Column(length = 50) private String province;
    @Column(length = 50) private String city;
    @Column(length = 50) private String district;
    @Column(length = 300) private String detail;
    @Column(name = "is_default") private Boolean isDefault = false;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public UserAddress() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String u) { this.username = u; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public String getPhone() { return phone; } public void setPhone(String p) { this.phone = p; }
    public String getProvince() { return province; } public void setProvince(String p) { this.province = p; }
    public String getCity() { return city; } public void setCity(String c) { this.city = c; }
    public String getDistrict() { return district; } public void setDistrict(String d) { this.district = d; }
    public String getDetail() { return detail; } public void setDetail(String d) { this.detail = d; }
    public Boolean getIsDefault() { return isDefault; } public void setIsDefault(Boolean i) { this.isDefault = i; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

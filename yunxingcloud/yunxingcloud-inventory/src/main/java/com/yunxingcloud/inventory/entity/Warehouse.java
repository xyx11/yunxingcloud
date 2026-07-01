package com.yunxingcloud.inventory.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "warehouse")
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 300) private String address;
    @Column(length = 1) private String status = "0";
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public Warehouse() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

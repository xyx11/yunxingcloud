package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "coupon")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 20, nullable = false) private String type;
    @Column(nullable = false) private Long threshold;
    @Column(nullable = false) private Long amount;
    @Column(name = "total_qty") private Integer totalQty = 100;
    @Column(name = "used_qty") private Integer usedQty = 0;
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;
    @Column(length = 1) private String status = "0";
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public Coupon() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public String getType() { return type; } public void setType(String t) { this.type = t; }
    public Long getThreshold() { return threshold; } public void setThreshold(Long t) { this.threshold = t; }
    public Long getAmount() { return amount; } public void setAmount(Long a) { this.amount = a; }
    public Integer getTotalQty() { return totalQty; } public void setTotalQty(Integer t) { this.totalQty = t; }
    public Integer getUsedQty() { return usedQty; } public void setUsedQty(Integer u) { this.usedQty = u; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime s) { this.startTime = s; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime e) { this.endTime = e; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

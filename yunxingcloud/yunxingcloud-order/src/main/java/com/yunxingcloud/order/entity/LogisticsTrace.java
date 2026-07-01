package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 物流轨迹 */
@Entity @Table(name = "logistics_trace")
public class LogisticsTrace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(length = 100) private String carrier;
    @Column(name = "tracking_no", length = 50) private String trackingNo;
    @Column(length = 200) private String status;      // 运输中/已签收/异常
    @Column(length = 500) private String location;    // 当前位置
    @Column(length = 500) private String description; // 轨迹描述
    @Column(name = "trace_time") private LocalDateTime traceTime;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getCarrier() { return carrier; } public void setCarrier(String v) { carrier = v; }
    public String getTrackingNo() { return trackingNo; } public void setTrackingNo(String v) { trackingNo = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getLocation() { return location; } public void setLocation(String v) { location = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public LocalDateTime getTraceTime() { return traceTime; } public void setTraceTime(LocalDateTime v) { traceTime = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
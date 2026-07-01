package com.yunxingcloud.order.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name = "order_shipment")
public class OrderShipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(length = 50) private String carrier;
    @Column(name = "tracking_no", length = 100) private String trackingNo;
    @Column(length = 20) private String status = "shipped";
    @Column(name = "shipped_at") private LocalDateTime shippedAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    public OrderShipment() {}
    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long o) { this.orderId = o; }
    public String getCarrier() { return carrier; } public void setCarrier(String c) { this.carrier = c; }
    public String getTrackingNo() { return trackingNo; } public void setTrackingNo(String t) { this.trackingNo = t; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDateTime getShippedAt() { return shippedAt; } public void setShippedAt(LocalDateTime s) { this.shippedAt = s; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; } public void setDeliveredAt(LocalDateTime d) { this.deliveredAt = d; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}

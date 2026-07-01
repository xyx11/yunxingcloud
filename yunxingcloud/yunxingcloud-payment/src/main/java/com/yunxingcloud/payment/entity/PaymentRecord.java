package com.yunxingcloud.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_record")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(length = 20, nullable = false)
    private String type;

    @Column(length = 20, nullable = false)
    private String channel;

    @Column(columnDefinition = "TEXT")
    private String request;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column
    private Boolean success = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PaymentRecord() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

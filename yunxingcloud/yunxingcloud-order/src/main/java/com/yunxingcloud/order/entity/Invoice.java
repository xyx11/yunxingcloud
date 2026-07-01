package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 发票 */
@Entity @Table(name = "invoice")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "order_no", length = 30) private String orderNo;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 10, nullable = false) private String type;      // personal/company
    @Column(length = 200) private String title;     // 发票抬头
    @Column(length = 30) private String taxNo;      // 税号
    @Column(length = 200) private String email;     // 接收邮箱
    @Column(length = 1) private String status = "0"; // 0待开/1已开/2失败
    @Column(name = "invoice_no", length = 50) private String invoiceNo; // 发票号码
    @Column(name = "invoice_url", length = 500) private String invoiceUrl;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String v) { orderNo = v; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getTaxNo() { return taxNo; } public void setTaxNo(String v) { taxNo = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getInvoiceNo() { return invoiceNo; } public void setInvoiceNo(String v) { invoiceNo = v; }
    public String getInvoiceUrl() { return invoiceUrl; } public void setInvoiceUrl(String v) { invoiceUrl = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
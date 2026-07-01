package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 分享记录 (商品分享裂变) */
@Entity @Table(name = "share_record")
public class ShareRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String sharer;     // 分享者
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(length = 20) private String channel;  // wechat/weibo/qq/copy
    @Column(name = "click_count") private Long clickCount = 0L;  // 点击量
    @Column(name = "order_count") private Long orderCount = 0L;  // 转化订单
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSharer() { return sharer; } public void setSharer(String v) { sharer = v; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public String getChannel() { return channel; } public void setChannel(String v) { channel = v; }
    public Long getClickCount() { return clickCount; } public void setClickCount(Long v) { clickCount = v; }
    public Long getOrderCount() { return orderCount; } public void setOrderCount(Long v) { orderCount = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
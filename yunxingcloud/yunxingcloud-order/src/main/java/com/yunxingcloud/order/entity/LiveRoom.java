package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 直播间 */
@Entity @Table(name = "live_room")
public class LiveRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "cover_url", length = 500) private String coverUrl;
    @Column(name = "stream_url", length = 500) private String streamUrl;          // 推流地址
    @Column(name = "anchor_name", length = 100) private String anchorName;
    @Column(name = "product_ids", length = 1000) private String productIds;        // 逗号分隔
    @Column(name = "viewer_count") private Integer viewerCount = 0;
    @Column(length = 1) private String status = "0"; // 0-预告/1-直播中/2-已结束
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getCoverUrl() { return coverUrl; } public void setCoverUrl(String v) { coverUrl = v; }
    public String getStreamUrl() { return streamUrl; } public void setStreamUrl(String v) { streamUrl = v; }
    public String getAnchorName() { return anchorName; } public void setAnchorName(String v) { anchorName = v; }
    public String getProductIds() { return productIds; } public void setProductIds(String v) { productIds = v; }
    public Integer getViewerCount() { return viewerCount; } public void setViewerCount(Integer v) { viewerCount = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime v) { startTime = v; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime v) { endTime = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}

package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** CMS 文章 */
@Entity @Table(name = "cms_article")
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(length = 100) private String category;    // news/help/about
    @Column(length = 500) private String coverUrl;
    @Column(length = 500) private String tags;
    @Column(name = "view_count") private Long viewCount = 0L;
    @Column(length = 1) private String status = "0";   // 0草稿/1发布
    @Column(name = "publish_at") private LocalDateTime publishAt;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getContent() { return content; } public void setContent(String v) { content = v; }
    public String getCategory() { return category; } public void setCategory(String v) { category = v; }
    public String getCoverUrl() { return coverUrl; } public void setCoverUrl(String v) { coverUrl = v; }
    public String getTags() { return tags; } public void setTags(String v) { tags = v; }
    public Long getViewCount() { return viewCount; } public void setViewCount(Long v) { viewCount = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getPublishAt() { return publishAt; } public void setPublishAt(LocalDateTime v) { publishAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 客户反馈/建议 */
@Entity @Table(name = "feedback")
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 20, nullable = false) private String type;    // suggestion/bug/praise/complaint
    @Column(nullable = false, length = 500) private String content;
    @Column(length = 1000) private String images;   // 截图URL(逗号分隔)
    @Column(length = 50) private String contact;    // 联系方式
    @Column(length = 1) private String status = "0"; // 0未处理/1已处理/2已关闭
    @Column(length = 500) private String reply;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public String getContent() { return content; } public void setContent(String v) { content = v; }
    public String getImages() { return images; } public void setImages(String v) { images = v; }
    public String getContact() { return contact; } public void setContact(String v) { contact = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getReply() { return reply; } public void setReply(String v) { reply = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}

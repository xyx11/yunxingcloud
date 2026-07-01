package com.yunxingcloud.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 客服聊天消息 */
@Entity @Table(name = "chat_message")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String sender;
    @Column(nullable = false, length = 100) private String receiver; // 接收者或 'ADMIN'
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "session_id", length = 50) private String sessionId;
    @Column(name = "is_read") private Boolean isRead = false;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSender() { return sender; } public void setSender(String v) { sender = v; }
    public String getReceiver() { return receiver; } public void setReceiver(String v) { receiver = v; }
    public String getContent() { return content; } public void setContent(String v) { content = v; }
    public String getSessionId() { return sessionId; } public void setSessionId(String v) { sessionId = v; }
    public Boolean getIsRead() { return isRead; } public void setIsRead(Boolean v) { isRead = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
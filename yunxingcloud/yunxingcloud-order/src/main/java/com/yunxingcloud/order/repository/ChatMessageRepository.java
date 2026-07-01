package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<ChatMessage> findBySenderOrReceiverOrderByCreatedAtDesc(String sender, String receiver);
}
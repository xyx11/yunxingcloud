package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.ChatMessage;
import com.yunxingcloud.order.repository.ChatMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ChatController {

    private final ChatMessageRepository repo;
    private final SimpMessagingTemplate messaging;

    public ChatController(ChatMessageRepository repo, SimpMessagingTemplate messaging) {
        this.repo = repo; this.messaging = messaging;
    }

    /** WebSocket 发送消息 */
    @MessageMapping("/chat/send")
    public void send(@Payload ChatMessage msg, java.security.Principal principal) {
        msg.setSender(principal.getName());
        msg = repo.save(msg);
        // 推送给接收者
        messaging.convertAndSendToUser(msg.getReceiver(), "/queue/chat", msg);
        // 如果接收者是用户, 同步推送给所有管理员
        if (!"ADMIN".equals(msg.getReceiver())) {
            messaging.convertAndSend("/topic/admin/chat", msg);
        }
    }

    /** REST: 获取会话消息 */
    @GetMapping("/api/chat/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> messages(@PathVariable String sessionId) {
        return ResponseEntity.ok(repo.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    /** REST: 获取用户会话列表 */
    @GetMapping("/api/chat/sessions")
    @ResponseBody
    public ResponseEntity<?> mySessions() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ChatMessage> msgs = repo.findBySenderOrReceiverOrderByCreatedAtDesc(user, user);
        Set<String> sessions = new LinkedHashSet<>();
        for (ChatMessage m : msgs) if (m.getSessionId() != null) sessions.add(m.getSessionId());
        return ResponseEntity.ok(sessions);
    }
}
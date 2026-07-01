package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.ChatMessage;
import com.yunxingcloud.order.repository.ChatMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository repo;

    public ChatController(ChatMessageRepository repo) { this.repo = repo; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    /** 发送消息 */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody ChatMessage msg) {
        msg.setSender(user());
        if (msg.getSessionId() == null) msg.setSessionId(UUID.randomUUID().toString().substring(0,8));
        return ResponseEntity.ok(repo.save(msg));
    }

    /** 获取会话消息 */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> messages(@PathVariable String sessionId) {
        return ResponseEntity.ok(repo.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    /** 我的会话列表 */
    @GetMapping("/sessions")
    public ResponseEntity<?> mySessions() {
        String u = user();
        List<ChatMessage> msgs = repo.findBySenderOrReceiverOrderByCreatedAtDesc(u, u);
        Set<String> sessions = new LinkedHashSet<>();
        for (ChatMessage m : msgs) if (m.getSessionId() != null) sessions.add(m.getSessionId());
        return ResponseEntity.ok(sessions);
    }
}
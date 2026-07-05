package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.ChatMessage;
import com.yunxingcloud.order.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) { this.chatService = chatService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody ChatMessage msg) {
        msg.setSender(user());
        return ResponseEntity.ok(chatService.send(msg));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> messages(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.messages(sessionId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> mySessions() {
        return ResponseEntity.ok(chatService.sessions(user()));
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.Message;
import com.yunxingcloud.yunxingcloud.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) { this.messageService = messageService; }

    private String currentUser() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/inbox")
    public ResponseEntity<List<Message>> inbox() {
        return ResponseEntity.ok(messageService.inbox(currentUser()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Message>> sent() {
        return ResponseEntity.ok(messageService.sent(currentUser()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", messageService.unreadCount(currentUser())));
    }

    @PostMapping
    public ResponseEntity<Message> send(@RequestBody Message msg) {
        msg.setFromUser(currentUser());
        return ResponseEntity.ok(messageService.send(msg));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id) {
        return messageService.markRead(id, currentUser())
                .map(m -> m != null
                    ? ResponseEntity.ok(Map.<String, Object>of("success", true))
                    : ResponseEntity.status(403).body(Map.<String, Object>of("success", false)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return messageService.delete(id, currentUser())
                .map(ok -> ok
                    ? ResponseEntity.ok(Map.<String, Object>of("success", true))
                    : ResponseEntity.status(403).body(Map.<String, Object>of("success", false)))
                .orElse(ResponseEntity.notFound().build());
    }
}

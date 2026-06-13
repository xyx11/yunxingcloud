package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.Message;
import com.yunxingcloud.yunxingcloud.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository msgRepo;

    public MessageController(MessageRepository msgRepo) { this.msgRepo = msgRepo; }

    @GetMapping("/inbox")
    public ResponseEntity<List<Message>> inbox() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(msgRepo.findByToUserOrderByCreatedAtDesc(user));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Message>> sent() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(msgRepo.findByFromUserOrderByCreatedAtDesc(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(Map.of("count", msgRepo.countUnread(user)));
    }

    @PostMapping
    public ResponseEntity<Message> send(@RequestBody Message msg) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        msg.setFromUser(user);
        return ResponseEntity.ok(msgRepo.save(msg));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id) {
        msgRepo.findById(id).ifPresent(m -> { m.setIsRead(true); msgRepo.save(m); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        msgRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

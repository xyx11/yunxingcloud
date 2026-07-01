package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Notification;
import com.yunxingcloud.order.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> myNotifications() {
        return ResponseEntity.ok(repo.findByUsernameOrUsernameOrderByCreatedAtDesc(user(), "ALL"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount() {
        return ResponseEntity.ok(Map.of("count", repo.countByUsernameAndIsReadFalse(user())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        repo.findById(id).ifPresent(n -> { n.setIsRead(true); n.setReadAt(LocalDateTime.now()); repo.save(n); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead() {
        repo.findByUsernameOrUsernameOrderByCreatedAtDesc(user(), "ALL").stream()
                .filter(n -> !n.getIsRead())
                .forEach(n -> { n.setIsRead(true); n.setReadAt(LocalDateTime.now()); repo.save(n); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> send(@RequestBody Notification notif) {
        return ResponseEntity.ok(repo.save(notif));
    }
}
package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Notification;
import com.yunxingcloud.order.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> myNotifications() {
        return ResponseEntity.ok(notificationService.get(user()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(user())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead() {
        notificationService.markAllRead(user());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> send(@RequestBody Notification notif) {
        return ResponseEntity.ok(notificationService.send(notif));
    }
}

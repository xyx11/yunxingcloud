package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Feedback;
import com.yunxingcloud.order.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) { this.feedbackService = feedbackService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> "admin".equals(a.getAuthority())); }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Feedback fb) {
        fb.setUsername(user());
        return ResponseEntity.ok(feedbackService.submit(fb));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(feedbackService.list(isAdmin(), user()));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        feedbackService.reply(id, body.get("reply"));
        return ResponseEntity.ok(Map.of("success", true));
    }
}

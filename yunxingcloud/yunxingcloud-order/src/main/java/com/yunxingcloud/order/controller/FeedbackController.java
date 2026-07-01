package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Feedback;
import com.yunxingcloud.order.repository.FeedbackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository repo;

    public FeedbackController(FeedbackRepository repo) { this.repo = repo; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> "admin".equals(a.getAuthority())); }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Feedback fb) {
        fb.setUsername(user());
        return ResponseEntity.ok(repo.save(fb));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        if (isAdmin()) return ResponseEntity.ok(repo.findByOrderByCreatedAtDesc());
        return ResponseEntity.ok(repo.findByUsernameOrderByCreatedAtDesc(user()));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        repo.findById(id).ifPresent(f -> { f.setReply(body.get("reply")); f.setStatus("1"); repo.save(f); });
        return ResponseEntity.ok(Map.of("success", true));
    }
}

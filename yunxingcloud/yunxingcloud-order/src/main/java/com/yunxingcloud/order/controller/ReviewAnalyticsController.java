package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ReviewAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价分析", description = "商品评价分析")
@RestController
@RequestMapping("/api/reviews")
public class ReviewAnalyticsController {

    private final ReviewAnalyticsService service;

    public ReviewAnalyticsController(ReviewAnalyticsService service) { this.service = service; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/my")
    public ResponseEntity<?> myReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.myReviews(user(), page, size));
    }

    @GetMapping("/summary/{productId}")
    public ResponseEntity<?> productSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(service.summary(productId));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> siteStats() {
        return ResponseEntity.ok(service.siteStats());
    }

    @GetMapping("/latest")
    public ResponseEntity<?> latest() {
        return ResponseEntity.ok(java.util.Map.of("message", "Use /summary/{productId} for per-product reviews"));
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() { return ResponseEntity.ok(java.util.Map.of("status", "ok")); }
}
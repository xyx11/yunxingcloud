package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ReviewAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewAnalyticsController {

    private final ReviewAnalyticsService service;

    public ReviewAnalyticsController(ReviewAnalyticsService service) { this.service = service; }

    @GetMapping("/summary/{productId}")
    public ResponseEntity<?> productSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(service.summary(productId));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> siteStats() {
        return ResponseEntity.ok(service.siteStats());
    }
}
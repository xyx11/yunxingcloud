package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ReviewAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价分析", description = "商品评价分析")
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
package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据分析", description = "商城数据分析")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping("/sales-overview")
    public ResponseEntity<?> salesOverview() { return ResponseEntity.ok(service.salesOverview()); }

    @GetMapping("/order-trend")
    public ResponseEntity<?> orderTrend() { return ResponseEntity.ok(service.orderTrend()); }

    @GetMapping("/top-products")
    public ResponseEntity<?> topProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.topProducts(limit));
    }
}
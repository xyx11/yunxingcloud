package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
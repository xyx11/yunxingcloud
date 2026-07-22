package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "营收管理", description = "营收数据与报表")
@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService service;

    public RevenueController(RevenueService service) { this.service = service; }

    @GetMapping("/overview")
    public ResponseEntity<?> overview() { return ResponseEntity.ok(service.overview()); }

    @GetMapping("/daily")
    public ResponseEntity<?> daily() { return ResponseEntity.ok(service.dailyRevenue()); }

    @GetMapping("/monthly")
    public ResponseEntity<?> monthly() {
        var overview = service.overview();
        var daily = service.dailyRevenue();
        return ResponseEntity.ok(java.util.Map.of("overview", overview, "daily", daily));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() { return ResponseEntity.ok(service.overview()); }
}
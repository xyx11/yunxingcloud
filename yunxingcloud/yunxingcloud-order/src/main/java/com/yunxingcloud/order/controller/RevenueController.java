package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.RevenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService service;

    public RevenueController(RevenueService service) { this.service = service; }

    @GetMapping("/overview")
    public ResponseEntity<?> overview() { return ResponseEntity.ok(service.overview()); }

    @GetMapping("/daily")
    public ResponseEntity<?> daily() { return ResponseEntity.ok(service.dailyRevenue()); }
}
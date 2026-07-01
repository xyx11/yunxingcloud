package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.LogisticsTrace;
import com.yunxingcloud.order.repository.LogisticsTraceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsTraceRepository repo;

    public LogisticsController(LogisticsTraceRepository repo) { this.repo = repo; }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> traces(@PathVariable Long orderId) {
        return ResponseEntity.ok(repo.findByOrderIdOrderByTraceTimeDesc(orderId));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody LogisticsTrace trace) {
        return ResponseEntity.ok(repo.save(trace));
    }

    @GetMapping("/track/{trackingNo}")
    public ResponseEntity<?> track(@PathVariable String trackingNo) {
        return ResponseEntity.ok(repo.findByTrackingNoOrderByTraceTimeDesc(trackingNo));
    }
}
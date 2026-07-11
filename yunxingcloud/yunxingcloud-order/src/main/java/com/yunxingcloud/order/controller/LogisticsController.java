package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.LogisticsTrace;
import com.yunxingcloud.order.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "物流管理", description = "物流跟踪与配送")
@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) { this.logisticsService = logisticsService; }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> traces(@PathVariable Long orderId) {
        return ResponseEntity.ok(logisticsService.tracesByOrder(orderId));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody LogisticsTrace trace) {
        return ResponseEntity.ok(logisticsService.add(trace));
    }

    @GetMapping("/track/{trackingNo}")
    public ResponseEntity<?> track(@PathVariable String trackingNo) {
        return ResponseEntity.ok(logisticsService.track(trackingNo));
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据统计", description = "仪表盘数据与统计图表")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(statsService.getDashboard());
    }

    @GetMapping("/recent-notices")
    public ResponseEntity<List<SysNotice>> recentNotices() {
        return ResponseEntity.ok(statsService.getRecentNotices());
    }
}

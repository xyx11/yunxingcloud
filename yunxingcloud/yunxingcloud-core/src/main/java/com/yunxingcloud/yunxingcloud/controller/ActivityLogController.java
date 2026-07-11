package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "活动日志", description = "用户活动日志查询")
@RestController
@RequestMapping("/api/activity")
public class ActivityLogController {

    private final List<Map<String, Object>> activities = new ArrayList<>();

    public ActivityLogController() {
        log("system", "平台启动", "v2.3.0 已就绪");
    }

    public void log(String user, String action, String detail) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("user", user); entry.put("action", action);
        entry.put("detail", detail); entry.put("time", LocalDateTime.now().toString());
        activities.add(0, entry);
        if (activities.size() > 500) activities.remove(activities.size() - 1);
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(activities.stream().limit(limit).toList());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, String> body) {
        log(body.getOrDefault("user", "admin"), body.get("action"), body.getOrDefault("detail", ""));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
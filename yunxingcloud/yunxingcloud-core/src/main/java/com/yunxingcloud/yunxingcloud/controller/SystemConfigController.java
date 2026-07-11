package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统配置", description = "系统配置管理")
@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {

    private final Map<String, Object> config = new LinkedHashMap<>();

    public SystemConfigController() {
        config.put("siteName", "YXCLOUD 商城");
        config.put("siteDescription", "分布式微服务电商平台");
        config.put("maintenanceMode", false);
        config.put("maxUploadSize", "10MB");
        config.put("orderTimeoutMinutes", 15);
        config.put("pointsRatio", 100);
        config.put("version", "v2.3.0");
        config.put("updatedAt", LocalDateTime.now().toString());
    }

    @GetMapping
    public ResponseEntity<?> getAll() { return ResponseEntity.ok(config); }

    @GetMapping("/{key}")
    public ResponseEntity<?> get(@PathVariable String key) {
        return ResponseEntity.ok(Map.of(key, config.getOrDefault(key, null)));
    }

    @PutMapping("/{key}")
    public ResponseEntity<?> set(@PathVariable String key, @RequestBody Map<String, Object> body) {
        config.put(key, body.get("value"));
        config.put("updatedAt", LocalDateTime.now().toString());
        return ResponseEntity.ok(Map.of(key, config.get(key)));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<?> toggleMaintenance(@RequestBody Map<String, Boolean> body) {
        config.put("maintenanceMode", body.getOrDefault("maintenance", false));
        return ResponseEntity.ok(Map.of("maintenanceMode", config.get("maintenanceMode")));
    }
}
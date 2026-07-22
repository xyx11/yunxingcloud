package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ProductCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Map;

@Tag(name = "系统健康", description = "服务健康检查")
@RestController
public class HealthController {

    private final JdbcTemplate jdbc;
    private final ProductCacheService cacheService;

    @Value("${spring.application.name:yunxingcloud-order}")
    private String appName;

    @Value("${app.version:2.5.0}")
    private String appVersion;

    public HealthController(JdbcTemplate jdbc, ProductCacheService cacheService) {
        this.jdbc = jdbc;
        this.cacheService = cacheService;
    }

    @GetMapping("/api/health")
    @Operation(summary = "健康检查")
    public ResponseEntity<?> health() {
        boolean dbOk = checkDatabase();
        boolean diskOk = checkDisk();

        return ResponseEntity.ok(Map.of(
            "status", dbOk && diskOk ? "UP" : "DEGRADED",
            "service", appName,
            "version", appVersion,
            "uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s",
            "checks", Map.of(
                "database", dbOk ? "UP" : "DOWN",
                "disk", diskOk ? "UP" : "DOWN"
            )
        ));
    }

    private boolean checkDatabase() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDisk() {
        try {
            File tmp = new File(System.getProperty("java.io.tmpdir"));
            return tmp.getUsableSpace() > 10 * 1024 * 1024;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/api/ping")
    @Operation(summary = "Ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("pong", true, "timestamp", System.currentTimeMillis()));
    }

    @GetMapping("/api/info")
    @Operation(summary = "服务信息")
    public ResponseEntity<?> info() {
        return ResponseEntity.ok(Map.of(
            "service", appName,
            "version", appVersion,
            "java", System.getProperty("java.version"),
            "os", System.getProperty("os.name") + " " + System.getProperty("os.arch"),
            "availableProcessors", Runtime.getRuntime().availableProcessors(),
            "maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB"
        ));
    }

    @GetMapping("/api/cache/refresh")
    @Operation(summary = "刷新所有缓存")
    public ResponseEntity<?> refreshCache() {
        return ResponseEntity.ok(cacheService.refreshAllCaches());
    }

    @GetMapping("/actuator/health/liveness")
    @Operation(summary = "K8s Liveness Probe")
    public ResponseEntity<?> liveness() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/actuator/health/readiness")
    @Operation(summary = "K8s Readiness Probe")
    public ResponseEntity<?> readiness() {
        boolean dbOk = checkDatabase();
        return ResponseEntity.status(dbOk ? 200 : 503)
                .body(Map.of("status", dbOk ? "UP" : "DOWN", "database", dbOk ? "UP" : "DOWN"));
    }

    @GetMapping("/actuator/metrics")
    @Operation(summary = "Prometheus Metrics")
    public ResponseEntity<?> metrics() {
        var rt = Runtime.getRuntime();
        return ResponseEntity.ok(Map.of(
            "jvm.memory.used", rt.totalMemory() - rt.freeMemory(),
            "jvm.memory.max", rt.maxMemory(),
            "jvm.threads", Thread.activeCount(),
            "jvm.uptime.seconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000
        ));
    }

    @GetMapping("/api/system/overview")
    @Operation(summary = "系统总览")
    public ResponseEntity<?> overview() {
        return ResponseEntity.ok(Map.of(
            "service", appName, "version", appVersion,
            "controllers", 38, "services", 42, "tests", 274,
            "uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s"
        ));
    }
}
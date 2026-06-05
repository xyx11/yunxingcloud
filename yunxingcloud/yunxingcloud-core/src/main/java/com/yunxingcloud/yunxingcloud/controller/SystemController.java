package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.yunxingcloud.config.TokenStore;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final CacheManager cacheManager;
    private final TokenStore tokenStore;
    private final FeatureFlags featureFlags;

    public SystemController(CacheManager cacheManager, TokenStore tokenStore, FeatureFlags featureFlags) {
        this.cacheManager = cacheManager;
        this.tokenStore = tokenStore;
        this.featureFlags = featureFlags;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        var runtime = Runtime.getRuntime();
        var mem = ManagementFactory.getMemoryMXBean();
        var uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startTime", LocalDateTime.now()
                .minus(Duration.ofMillis(uptime))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("uptime", formatDuration(uptime));
        data.put("availableProcessors", runtime.availableProcessors());
        data.put("totalMemory", formatBytes(runtime.totalMemory()));
        data.put("freeMemory", formatBytes(runtime.freeMemory()));
        data.put("maxMemory", formatBytes(runtime.maxMemory()));
        data.put("heapUsed", formatBytes(mem.getHeapMemoryUsage().getUsed()));
        data.put("heapMax", formatBytes(mem.getHeapMemoryUsage().getMax()));
        data.put("osName", System.getProperty("os.name"));
        data.put("javaVersion", System.getProperty("java.version"));

        return ResponseEntity.ok(data);
    }

    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> cacheInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(name);
            data.put(name, "active");
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> sessions() {
        return ResponseEntity.ok(Map.of("count", tokenStore.count(), "sessions", tokenStore.activeSessions()));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/sessions/revoke")
    public ResponseEntity<Map<String, Object>> revokeSession(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username != null) {
            tokenStore.revokeByUser(username);
            return ResponseEntity.ok(Map.of("success", true, "message", "已强制下线: " + username));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少 username"));
    }

    @GetMapping("/flags")
    public ResponseEntity<Map<String, Object>> flags() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("features", featureFlags.getAll());
        data.put("announcement", featureFlags.getAnnouncement());
        return ResponseEntity.ok(data);
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        for (String name : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "缓存已清除"));
    }

    private String formatDuration(long millis) {
        long days = millis / 86400000;
        long hours = (millis % 86400000) / 3600000;
        long mins = (millis % 3600000) / 60000;
        return String.format("%dd %dh %dm", days, hours, mins);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}

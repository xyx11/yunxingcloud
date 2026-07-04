package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.config.TokenStore;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.lang.management.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final CacheManager cacheManager;
    private final TokenStore tokenStore;
    private final FeatureFlags featureFlags;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final I18nService i18n;
    private final List<Map<String, Object>> history = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 60;

    public SystemController(CacheManager cacheManager, TokenStore tokenStore,
                            FeatureFlags featureFlags, JdbcTemplate jdbcTemplate,
                            DataSource dataSource, I18nService i18n) {
        this.cacheManager = cacheManager;
        this.tokenStore = tokenStore;
        this.featureFlags = featureFlags;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.i18n = i18n;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        var runtime = Runtime.getRuntime();
        var mem = ManagementFactory.getMemoryMXBean();
        var osBean = ManagementFactory.getOperatingSystemMXBean();
        var uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        var threadBean = ManagementFactory.getThreadMXBean();

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
        data.put("heapUsedRaw", mem.getHeapMemoryUsage().getUsed());
        data.put("heapMaxRaw", mem.getHeapMemoryUsage().getMax());
        data.put("osName", System.getProperty("os.name"));
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("threadCount", threadBean.getThreadCount());
        data.put("peakThreadCount", threadBean.getPeakThreadCount());

        // CPU load
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            data.put("cpuLoad", String.format("%.1f%%", sunOsBean.getCpuLoad() * 100));
            data.put("cpuLoadRaw", sunOsBean.getCpuLoad());
        } else {
            data.put("cpuLoad", "N/A");
            data.put("cpuLoadRaw", 0.0);
        }

        // GC info
        long gcCount = 0, gcTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcCount += gcBean.getCollectionCount();
            gcTime += gcBean.getCollectionTime();
        }
        data.put("gcCount", gcCount);
        data.put("gcTimeMs", gcTime);

        // HikariCP pool info
        try {
            if (dataSource instanceof HikariDataSource hds) {
                var pool = hds.getHikariPoolMXBean();
                if (pool != null) {
                    data.put("dbActive", pool.getActiveConnections());
                    data.put("dbIdle", pool.getIdleConnections());
                    data.put("dbTotal", pool.getTotalConnections());
                    data.put("dbWaiting", pool.getThreadsAwaitingConnection());
                }
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok(data);
    }

    @PreAuthorize("hasAuthority('config:read')")
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> cacheInfo() {
        List<Map<String, Object>> details = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (String name : cacheManager.getCacheNames()) {
            names.add(name);
            var cache = cacheManager.getCache(name);
            details.add(Map.of("name", name, "status", "active"));
        }
        return ResponseEntity.ok(Map.of("cacheNames", names, "details", details));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> history() {
        // Add current snapshot
        var mem = ManagementFactory.getMemoryMXBean();
        var osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("time", Instant.now().toString());
        snap.put("heapUsed", mem.getHeapMemoryUsage().getUsed());
        snap.put("sessions", tokenStore.count());
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            snap.put("cpuLoad", sunOsBean.getCpuLoad());
        }
        history.add(snap);
        if (history.size() > MAX_HISTORY) history.remove(0);
        return ResponseEntity.ok(new ArrayList<>(history));
    }

    @PreAuthorize("hasAuthority('config:read')")
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
            return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("system.revoke_success", username)));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("system.username_required")));
    }

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> benchmark() {
        long start = System.currentTimeMillis();
        Map<String, Object> r = new LinkedHashMap<>();
        try { jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class); r.put("db_ms", System.currentTimeMillis() - start); } catch (Exception e) { r.put("db_ms", -1); }
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) cacheManager.getCache("menuTree");
        r.put("cache_1k_ms", System.currentTimeMillis() - t2);
        r.put("total_ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/flags")
    public ResponseEntity<Map<String, Object>> flags() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("features", featureFlags.getAll());
        data.put("announcement", featureFlags.getAnnouncement());
        return ResponseEntity.ok(data);
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/flags")
    public ResponseEntity<Map<String, Object>> updateFlags(@RequestBody Map<String, String> body) {
        if (body.containsKey("announcement")) {
            jdbcTemplate.update("DELETE FROM sys_config WHERE config_key = 'sys.announcement'");
            jdbcTemplate.update("INSERT INTO sys_config (name, config_key, config_value, config_type) VALUES (?,?,?,?)",
                    "系统公告", "sys.announcement", body.get("announcement"), "Y");
            featureFlags.refresh();
        }
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("system.announcement_updated")));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        for (String name : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("system.cache_cleared")));
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

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new LinkedHashMap<>();
        int[][] services = {{8080,0},{8081,0},{8083,0},{8084,0},{8085,0},{8090,0}};
        String[] names = {"core","usercenter","payment","order","inventory","gateway"};
        for (int i = 0; i < services.length; i++) {
            try {
                java.net.URL url = new java.net.URL("http://127.0.0.1:" + services[i][0] + "/actuator/health");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000); conn.setReadTimeout(2000);
                status.put(names[i], conn.getResponseCode() == 200 ? "UP" : "DOWN");
                conn.disconnect();
            } catch (Exception e) { status.put(names[i], "DOWN"); }
        }
        return ResponseEntity.ok(status);
    }
}

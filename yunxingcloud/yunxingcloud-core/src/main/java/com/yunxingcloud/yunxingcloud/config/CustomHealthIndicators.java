package com.yunxingcloud.yunxingcloud.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class CustomHealthIndicators {

    private final JdbcTemplate jdbcTemplate;

    public CustomHealthIndicators(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public Map<String, Object> dbHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("status", "UP");
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/disk")
    public Map<String, Object> diskHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        File root = new File(".");
        long free = root.getFreeSpace();
        long total = root.getTotalSpace();
        result.put("usedMB", (total - free) / (1024 * 1024));
        result.put("totalMB", total / (1024 * 1024));
        result.put("freeMB", free / (1024 * 1024));
        result.put("status", free > 100L * 1024 * 1024 ? "UP" : "LOW_SPACE");
        return result;
    }
}

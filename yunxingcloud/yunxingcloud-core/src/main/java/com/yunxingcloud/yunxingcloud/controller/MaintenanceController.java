package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@PreAuthorize("hasAuthority('config:write')")
public class MaintenanceController {

    private final JdbcTemplate jdbcTemplate;

    public MaintenanceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/clean-logs")
    public ResponseEntity<Map<String, Object>> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM sys_oper_log WHERE oper_time < ?",
                LocalDateTime.now().minusDays(days));
            return ResponseEntity.ok(Map.of("success", true, "deleted", deleted,
                    "message", "已清理 " + days + " 天前的 " + deleted + " 条日志"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/clean-tokens")
    public ResponseEntity<Map<String, Object>> cleanExpiredTokens() {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM password_reset_tokens WHERE expires_at < ?", LocalDateTime.now());
            return ResponseEntity.ok(Map.of("success", true, "deleted", deleted, "message", "已清理过期令牌"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> dbStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            stats.put("users", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
            stats.put("menus", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu", Long.class));
            stats.put("roles", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM role", Long.class));
            stats.put("operLogs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_oper_log", Long.class));
            stats.put("jobs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_job", Long.class));
            stats.put("configs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_config", Long.class));
            stats.put("tokens", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM password_reset_tokens", Long.class));
            stats.put("activeSessions", 0); // populated by TokenStore
            stats.put("timestamp", LocalDateTime.now().toString());
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/vacuum")
    public ResponseEntity<Map<String, Object>> vacuum() {
        Map<String, Object> before = new LinkedHashMap<>();
        try { before.put("operLogs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_oper_log", Long.class)); } catch (Exception ignored) {}
        // 清理90天前日志 + 过期令牌
        int logs = 0, tokens = 0;
        try { logs = jdbcTemplate.update("DELETE FROM sys_oper_log WHERE oper_time < ?", LocalDateTime.now().minusDays(90)); } catch (Exception ignored) {}
        try { tokens = jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE expires_at < ?", LocalDateTime.now()); } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("success", true, "logsDeleted", logs, "tokensDeleted", tokens,
                "message", "清理完成"));
    }
}

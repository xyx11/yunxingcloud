package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
@PreAuthorize("hasAuthority('config:write')")
public class MaintenanceController {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);
    private final JdbcTemplate jdbcTemplate;
    private final I18nService i18n;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/yunxingcloud_core}")
    private String dbUrl;

    public MaintenanceController(JdbcTemplate jdbcTemplate, I18nService i18n) {
        this.jdbcTemplate = jdbcTemplate;
        this.i18n = i18n;
    }

    private String getDbName() {
        String url = dbUrl;
        int idx = url.lastIndexOf('/');
        if (idx >= 0) {
            String db = url.substring(idx + 1);
            int q = db.indexOf('?');
            return q >= 0 ? db.substring(0, q) : db;
        }
        return "yunxingcloud_core";
    }

    @PostMapping("/clean-logs")
    public ResponseEntity<Map<String, Object>> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM sys_oper_log WHERE oper_time < ?",
                LocalDateTime.now().minusDays(days));
            return ResponseEntity.ok(Map.of("success", true, "deleted", deleted,
                    "message", i18n.msg("maintenance.log_cleaned", days, deleted)));
        } catch (Exception e) {
            log.warn("清理日志失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/clean-tokens")
    public ResponseEntity<Map<String, Object>> cleanExpiredTokens() {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM password_reset_tokens WHERE expires_at < ?", LocalDateTime.now());
            return ResponseEntity.ok(Map.of("success", true, "deleted", deleted, "message", i18n.msg("maintenance.token_cleaned")));
        } catch (Exception e) {
            log.warn("清理令牌失败: {}", e.getMessage());
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
            stats.put("activeSessions", 0);
            stats.put("timestamp", LocalDateTime.now().toString());
        } catch (Exception e) {
            log.warn("获取数据库统计失败: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/backup")
    public ResponseEntity<Map<String, Object>> backup() {
        try {
            Path backupDir = Paths.get("uploads/backup");
            Files.createDirectories(backupDir);
            String filename = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
            Path file = backupDir.resolve(filename);
            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump", "-u" + dbUser, "-p" + dbPassword, "--databases", getDbName(),
                "--result-file=" + file.toAbsolutePath());
            Process p = pb.start();
            int exit = p.waitFor();
            long size = Files.size(file);
            return ResponseEntity.ok(Map.of("success", exit == 0, "filename", filename, "size", size,
                "message", exit == 0 ? i18n.msg("maintenance.backup_success", size/1024) : i18n.msg("maintenance.backup_failed")));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/backups")
    public ResponseEntity<List<Map<String, Object>>> listBackups() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Path backupDir = Paths.get("uploads/backup");
            if (Files.exists(backupDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, "*.sql")) {
                    for (Path p : stream) {
                        list.add(Map.of("filename", p.getFileName().toString(),
                            "size", Files.size(p),
                            "modified", Files.getLastModifiedTime(p).toString()));
                    }
                }
            }
            list.sort((a, b) -> b.get("filename").toString().compareTo(a.get("filename").toString()));
        } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/restore/{filename}")
    public ResponseEntity<Map<String, Object>> restore(@PathVariable String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("common.bad_request")));
        }
        try {
            Path file = Paths.get("uploads/backup/" + filename);
            if (!Files.exists(file)) return ResponseEntity.ok(Map.of("success", false, "message", i18n.msg("common.not_found")));
            ProcessBuilder pb = new ProcessBuilder("mysql", "-u" + dbUser, "-p" + dbPassword, getDbName());
            pb.redirectInput(file.toFile());
            Process p = pb.start();
            int exit = p.waitFor();
            return ResponseEntity.ok(Map.of("success", exit == 0, "message", exit == 0 ? i18n.msg("maintenance.restore_success") : i18n.msg("maintenance.restore_failed", exit)));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/backups/{filename}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads/backup/" + filename);
            Files.deleteIfExists(file);
            return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("maintenance.delete_success")));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/vacuum")
    public ResponseEntity<Map<String, Object>> vacuum() {
        Map<String, Object> before = new LinkedHashMap<>();
        try { before.put("operLogs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_oper_log", Long.class)); } catch (Exception e) { log.warn("统计日志数失败: {}", e.getMessage()); }
        int logs = 0, tokens = 0;
        try { logs = jdbcTemplate.update("DELETE FROM sys_oper_log WHERE oper_time < ?", LocalDateTime.now().minusDays(90)); } catch (Exception e) { log.warn("清理日志失败: {}", e.getMessage()); }
        try { tokens = jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE expires_at < ?", LocalDateTime.now()); } catch (Exception e) { log.warn("清理令牌失败: {}", e.getMessage()); }
        return ResponseEntity.ok(Map.of("success", true, "logsDeleted", logs, "tokensDeleted", tokens,
                "message", i18n.msg("maintenance.vacuum_complete")));
    }
}

package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);
    private final JdbcTemplate jdbcTemplate;
    private final I18nService i18n;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/yunxingcloud_core}")
    private String dbUrl;

    public MaintenanceService(JdbcTemplate jdbcTemplate, I18nService i18n) {
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

    @Transactional
    public Map<String, Object> cleanOldLogs(int days) {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM sys_oper_log WHERE oper_time < ?",
                LocalDateTime.now().minusDays(days));
            return Map.of("success", true, "deleted", deleted,
                    "message", i18n.msg("maintenance.log_cleaned", days, deleted));
        } catch (Exception e) {
            log.warn("清理日志失败: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> cleanExpiredTokens() {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM password_reset_tokens WHERE expires_at < ?", LocalDateTime.now());
            return Map.of("success", true, "deleted", deleted,
                    "message", i18n.msg("maintenance.token_cleaned"));
        } catch (Exception e) {
            log.warn("清理令牌失败: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    public Map<String, Object> getDbStats() {
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
        return stats;
    }

    public Map<String, Object> backup() {
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
            return Map.of("success", exit == 0, "filename", filename, "size", size,
                "message", exit == 0 ? i18n.msg("maintenance.backup_success", size / 1024)
                        : i18n.msg("maintenance.backup_failed"));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    public List<Map<String, Object>> listBackups() {
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
        } catch (Exception ignored) {
            log.warn("操作异常: {}", ignored.getMessage());
        }
        return list;
    }

    public Map<String, Object> restore(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Map.of("success", false, "message", i18n.msg("common.bad_request"));
        }
        try {
            Path file = Paths.get("uploads/backup/" + filename);
            if (!Files.exists(file)) return Map.of("success", false, "message", i18n.msg("common.not_found"));
            ProcessBuilder pb = new ProcessBuilder("mysql", "-u" + dbUser, "-p" + dbPassword, getDbName());
            pb.redirectInput(file.toFile());
            Process p = pb.start();
            int exit = p.waitFor();
            return Map.of("success", exit == 0, "message",
                    exit == 0 ? i18n.msg("maintenance.restore_success")
                            : i18n.msg("maintenance.restore_failed", exit));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    public Map<String, Object> deleteBackup(String filename) {
        try {
            Path file = Paths.get("uploads/backup/" + filename);
            Files.deleteIfExists(file);
            return Map.of("success", true, "message", i18n.msg("maintenance.delete_success"));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> vacuum() {
        Map<String, Object> before = new LinkedHashMap<>();
        try {
            before.put("operLogs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_oper_log", Long.class));
        } catch (Exception e) {
            log.warn("统计日志数失败: {}", e.getMessage());
        }
        int logs = 0, tokens = 0;
        try {
            logs = jdbcTemplate.update("DELETE FROM sys_oper_log WHERE oper_time < ?",
                    LocalDateTime.now().minusDays(90));
        } catch (Exception e) {
            log.warn("清理日志失败: {}", e.getMessage());
        }
        try {
            tokens = jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE expires_at < ?",
                    LocalDateTime.now());
        } catch (Exception e) {
            log.warn("清理令牌失败: {}", e.getMessage());
        }
        return Map.of("success", true, "logsDeleted", logs, "tokensDeleted", tokens,
                "message", i18n.msg("maintenance.vacuum_complete"));
    }
}

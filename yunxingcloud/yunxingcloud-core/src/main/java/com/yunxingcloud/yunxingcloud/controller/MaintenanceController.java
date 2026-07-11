package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统维护", description = "备份恢复/日志清理/数据库统计")
@RestController
@RequestMapping("/api/maintenance")
@PreAuthorize("hasAuthority('config:write')")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/clean-logs")
    public ResponseEntity<Map<String, Object>> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(maintenanceService.cleanOldLogs(days));
    }

    @PostMapping("/clean-tokens")
    public ResponseEntity<Map<String, Object>> cleanExpiredTokens() {
        return ResponseEntity.ok(maintenanceService.cleanExpiredTokens());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> dbStats() {
        return ResponseEntity.ok(maintenanceService.getDbStats());
    }

    @PostMapping("/backup")
    public ResponseEntity<Map<String, Object>> backup() {
        return ResponseEntity.ok(maintenanceService.backup());
    }

    @GetMapping("/backups")
    public ResponseEntity<List<Map<String, Object>>> listBackups() {
        return ResponseEntity.ok(maintenanceService.listBackups());
    }

    @PostMapping("/restore/{filename}")
    public ResponseEntity<Map<String, Object>> restore(@PathVariable String filename) {
        Map<String, Object> result = maintenanceService.restore(filename);
        if (Boolean.FALSE.equals(result.get("success"))
                && result.get("message") != null
                && result.get("message").toString().contains("bad_request")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/backups/{filename}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable String filename) {
        return ResponseEntity.ok(maintenanceService.deleteBackup(filename));
    }

    @PostMapping("/vacuum")
    public ResponseEntity<Map<String, Object>> vacuum() {
        return ResponseEntity.ok(maintenanceService.vacuum());
    }
}

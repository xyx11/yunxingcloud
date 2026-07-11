package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.AuditTrail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "审计日志", description = "审计日志查询")
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditTrail auditTrail;

    public AuditController(AuditTrail auditTrail) { this.auditTrail = auditTrail; }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> recent(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditTrail.recent(limit));
    }

    @PostMapping
    public ResponseEntity<?> log(@RequestBody Map<String, String> body) {
        auditTrail.record(
                body.getOrDefault("entity", "system"),
                body.getOrDefault("action", "log"),
                body.getOrDefault("user", "admin"),
                body.getOrDefault("before", ""),
                body.getOrDefault("after", ""));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.SystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统监控", description = "系统信息/缓存/会话/健康检查")
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(systemService.getSystemInfo());
    }

    @PreAuthorize("hasAuthority('config:read')")
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> cacheInfo() {
        return ResponseEntity.ok(systemService.getCacheInfo());
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        systemService.addSnapshot();
        return ResponseEntity.ok(systemService.getHistory());
    }

    @PreAuthorize("hasAuthority('config:read')")
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> sessions() {
        return ResponseEntity.ok(systemService.getSessions());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/sessions/revoke")
    public ResponseEntity<Map<String, Object>> revokeSession(@RequestBody Map<String, String> body) {
        Map<String, Object> result = systemService.revokeSession(body.get("username"));
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> benchmark() {
        return ResponseEntity.ok(systemService.benchmark());
    }

    @GetMapping("/flags")
    public ResponseEntity<Map<String, Object>> flags() {
        return ResponseEntity.ok(systemService.getFlags());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/flags")
    public ResponseEntity<Map<String, Object>> updateFlags(@RequestBody Map<String, String> body) {
        if (body.containsKey("announcement")) {
            return ResponseEntity.ok(systemService.updateAnnouncement(body.get("announcement")));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        return ResponseEntity.ok(systemService.clearCache());
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(systemService.healthCheck());
    }
}

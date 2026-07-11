package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.service.LoginInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "登录日志", description = "系统登录日志的查询与清理")
@RestController
@RequestMapping("/api/logininfor")
public class LoginInfoController {

    private final LoginInfoService loginInfoService;

    public LoginInfoController(LoginInfoService loginInfoService) {
        this.loginInfoService = loginInfoService;
    }

    @Operation(summary = "登录统计")
    @PreAuthorize("hasAuthority('logininfor:read')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(loginInfoService.getStats());
    }

    @Operation(summary = "查询登录日志列表")
    @PreAuthorize("hasAuthority('logininfor:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(loginInfoService.list(userName, status, page, pageSize));
    }

    @Operation(summary = "删除登录日志")
    @PreAuthorize("hasAuthority('logininfor:write')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return loginInfoService.findById(id).map(info -> {
            loginInfoService.delete(info);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "清空登录日志")
    @PreAuthorize("hasAuthority('logininfor:write')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> clean() {
        return ResponseEntity.ok(loginInfoService.clean());
    }
}

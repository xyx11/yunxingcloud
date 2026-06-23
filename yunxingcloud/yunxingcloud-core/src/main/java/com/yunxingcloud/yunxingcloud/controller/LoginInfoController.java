package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysLoginInfo;
import com.yunxingcloud.yunxingcloud.repository.SysLoginInfoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map;

@Tag(name = "登录日志", description = "系统登录日志的查询与清理")
@RestController
@RequestMapping("/api/logininfor")
public class LoginInfoController {

    private final SysLoginInfoRepository loginInfoRepository;
    private final JdbcTemplate jdbc;
    private final I18nService i18n;

    public LoginInfoController(SysLoginInfoRepository loginInfoRepository, JdbcTemplate jdbc, I18nService i18n) {
        this.loginInfoRepository = loginInfoRepository;
        this.jdbc = jdbc;
        this.i18n = i18n;
    }

    @PreAuthorize("hasAuthority('logininfor:read')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long todaySuccess = 0, todayFail = 0, total = loginInfoRepository.count();
        try {
            String today = java.time.LocalDate.now().toString();
            todaySuccess = jdbc.queryForObject("SELECT COUNT(*) FROM sys_logininfor WHERE status='0' AND DATE(login_time)=?", Long.class, today);
            todayFail = jdbc.queryForObject("SELECT COUNT(*) FROM sys_logininfor WHERE status='1' AND DATE(login_time)=?", Long.class, today);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("total", total, "todaySuccess", todaySuccess, "todayFail", todayFail));
    }

    @PreAuthorize("hasAuthority('logininfor:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<SysLoginInfo> result;
        if (userName != null && !userName.isEmpty() && status != null && !status.isEmpty()) {
            result = loginInfoRepository.findByUserNameContainingAndStatus(userName, status, pageable);
        } else if (userName != null && !userName.isEmpty()) {
            result = loginInfoRepository.findByUserNameContaining(userName, pageable);
        } else if (status != null && !status.isEmpty()) {
            result = loginInfoRepository.findByStatus(status, pageable);
        } else {
            result = loginInfoRepository.findAll(pageable);
        }
        return ResponseEntity.ok(Map.of(
                "items", result.getContent(),
                "total", result.getTotalElements(),
                "page", page,
                "pageSize", pageSize
        ));
    }

    @PreAuthorize("hasAuthority('logininfor:write')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return loginInfoRepository.findById(id).map(info -> {
            loginInfoRepository.delete(info);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('logininfor:write')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> clean() {
        loginInfoRepository.deleteAll();
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("loginlog.cleaned")));
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.service.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "密码管理", description = "忘记密码/重置密码/修改密码")
@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordService passwordService;
    private final I18nService i18n;

    public PasswordController(PasswordService passwordService, I18nService i18n) {
        this.passwordService = passwordService;
        this.i18n = i18n;
    }

    @PostMapping("/forgot")
    @SentinelResource(value = "passwordForgotFlow", blockHandler = "forgotPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        Map<String, Object> result = passwordService.forgotPassword(body.get("email"));
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    @SentinelResource(value = "passwordResetFlow", blockHandler = "resetPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        Map<String, Object> result = passwordService.resetPassword(body.get("token"), body.get("newPassword"));
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Map<String, Object>> forgotPasswordBlockHandler(Map<String, String> body,
                                                                           BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", i18n.msg("password.email_failed")
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", i18n.msg("ratelimit.too_many_requests")
        ));
    }

    public ResponseEntity<Map<String, Object>> resetPasswordBlockHandler(Map<String, String> body,
                                                                          BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", i18n.msg("circuit_breaker.unavailable")
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", i18n.msg("ratelimit.too_many_requests")
        ));
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> result = passwordService.changePassword(username,
                body.get("oldPassword"), body.get("newPassword"));
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}

package com.yunxingcloud.usercenter.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.PasswordValidator;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "用户注册", description = "新用户注册")
@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;
    private final I18nService i18n;

    public RegisterController(UserService userService, I18nService i18n) {
        this.userService = userService;
        this.i18n = i18n;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @SentinelResource(value = "registerFlow", blockHandler = "registerBlockHandler")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("register.username_blank")));
        }
        List<String> pwErrors = PasswordValidator.validate(request.password());
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", i18n.msg("password.weak"),
                "details", String.join("; ", pwErrors)));
        }

        try {
            User user = userService.register(request.username(), request.password(), request.email());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", user.getUsername(),
                    "approved", user.isApproved(),
                    "message", user.isApproved() ? i18n.msg("register.success") : i18n.msg("register.pending_approval")
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", i18n.msg(e.getMessage())
            ));
        }
    }

    public ResponseEntity<Map<String, Object>> registerBlockHandler(RegisterRequest request,
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

    record RegisterRequest(String username, String password, String email) {}
}

package com.yunxingcloud.usercenter.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.PasswordValidator;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @SentinelResource(value = "registerFlow", blockHandler = "registerBlockHandler")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名不能为空"));
        }
        List<String> pwErrors = PasswordValidator.validate(request.password());
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", "密码强度不足",
                "details", String.join("; ", pwErrors)));
        }

        try {
            User user = userService.register(request.username(), request.password(), request.email());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", user.getUsername(),
                    "approved", user.isApproved(),
                    "message", user.isApproved() ? "注册成功" : "注册成功，请等待管理员审核"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    public ResponseEntity<Map<String, Object>> registerBlockHandler(RegisterRequest request,
                                                                      BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", "注册服务暂时不可用，请稍后重试"
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", "注册请求过于频繁，请稍后再试"
        ));
    }

    record RegisterRequest(String username, String password, String email) {}
}

package com.yunxingcloud.yunxingcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.PasswordValidator;
import com.yunxingcloud.yunxingcloud.entity.PasswordResetToken;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.PasswordResetTokenRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import com.yunxingcloud.yunxingcloud.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordController(UserRepository userRepository,
                               PasswordResetTokenRepository tokenRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/forgot")
    @SentinelResource(value = "passwordForgotFlow", blockHandler = "forgotPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请输入邮箱地址"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "如果该邮箱已注册，重置链接已发送"));
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), token);
        tokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(email, token);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            tokenRepository.delete(resetToken);
            return ResponseEntity.status(503).body(Map.of(
                    "success", false,
                    "message", "邮件服务暂时不可用，请稍后重试"
            ));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "重置链接已发送至邮箱"));
    }

    @PostMapping("/reset")
    @SentinelResource(value = "passwordResetFlow", blockHandler = "resetPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无效的重置令牌"));
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || !resetToken.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "重置令牌无效或已过期"));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", "密码强度不足",
                "details", String.join("; ", pwErrors)));
        }

        User user = userRepository.findById(resetToken.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return ResponseEntity.ok(Map.of("success", true, "message", "密码重置成功，请使用新密码登录"));
    }

    public ResponseEntity<Map<String, Object>> forgotPasswordBlockHandler(Map<String, String> body,
                                                                           BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", "邮件服务暂时不可用，请稍后重试"
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", "请求过于频繁，请稍后再试"
        ));
    }

    public ResponseEntity<Map<String, Object>> resetPasswordBlockHandler(Map<String, String> body,
                                                                          BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", "服务暂时不可用，请稍后重试"
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", "请求过于频繁，请稍后再试"
        ));
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请输入当前密码"));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请输入新密码"));
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "当前密码不正确"));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", "密码强度不足",
                "details", String.join("; ", pwErrors)));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
    }
}

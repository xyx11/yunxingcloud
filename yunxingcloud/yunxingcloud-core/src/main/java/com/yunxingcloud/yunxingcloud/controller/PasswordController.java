package com.yunxingcloud.yunxingcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.PasswordValidator;
import com.yunxingcloud.yunxingcloud.config.I18nService;
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
    private final I18nService i18n;

    public PasswordController(UserRepository userRepository,
                               PasswordResetTokenRepository tokenRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService,
                               I18nService i18n) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.i18n = i18n;
    }

    @PostMapping("/forgot")
    @SentinelResource(value = "passwordForgotFlow", blockHandler = "forgotPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.blank_email")));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("password.email_sent")));
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
                    "message", i18n.msg("password.email_failed")
            ));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("password.email_sent")));
    }

    @PostMapping("/reset")
    @SentinelResource(value = "passwordResetFlow", blockHandler = "resetPasswordBlockHandler")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.invalid_token")));
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || !resetToken.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.invalid_token")));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", i18n.msg("password.weak"),
                "details", String.join("; ", pwErrors)));
        }

        User user = userRepository.findById(resetToken.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.not_found")));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("password.reset_success")));
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
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("common.bad_request")));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("common.bad_request")));
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.not_found")));
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("password.old_wrong")));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", i18n.msg("password.weak"),
                "details", String.join("; ", pwErrors)));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("password.change_success")));
    }
}

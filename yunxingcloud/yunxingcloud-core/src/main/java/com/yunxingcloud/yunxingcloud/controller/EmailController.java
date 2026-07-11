package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "邮件管理", description = "邮件发送与测试")
@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final JavaMailSender mailSender;
    private final I18nService i18n;

    public EmailController(JavaMailSender mailSender, I18nService i18n) {
        this.mailSender = mailSender;
        this.i18n = i18n;
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> templates() {
        return ResponseEntity.ok(Map.of("templates", java.util.List.of(
            Map.of("key","welcome","name", i18n.msg("email.template_welcome"), "subject","Welcome to yunxingcloud","content","Hello {username},\n\nWelcome to yunxingcloud. Your account has been created."),
            Map.of("key","password_reset","name", i18n.msg("email.template_password_reset"), "subject","Password Reset Notification","content","Hello {username},\n\nYour password has been reset to {password}. Please change it after login."),
            Map.of("key","system_notice","name", i18n.msg("email.template_system_notice"), "subject","System Notification","content","Hello {username},\n\n{message}\n\nThis is an auto-generated email, please do not reply.")
        )));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> test(@RequestBody Map<String, String> body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(body.get("to"));
            msg.setSubject(body.getOrDefault("subject", "yunxingcloud 测试邮件"));
            String content = body.getOrDefault("content", "这是一封测试邮件，如果您收到说明邮件配置正确。");
            content = content.replace("{username}", body.getOrDefault("username", "用户")).replace("{message}", body.getOrDefault("message", ""));
            msg.setText(content);
            mailSender.send(msg);
            return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("email.send_success")));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", i18n.msg("email.send_failed", e.getMessage())));
        }
    }
}

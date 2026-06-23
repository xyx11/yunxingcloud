package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.I18nService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            Map.of("name","欢迎邮件","subject","欢迎加入 yunxingcloud","content","您好 {username}，\n\n欢迎使用 yunxingcloud 分布式微服务平台。\n\n您的账号已创建成功，请妥善保管。"),
            Map.of("name","密码重置","subject","密码重置通知","content","您好 {username}，\n\n您的密码已重置为 {password}，请登录后及时修改。"),
            Map.of("name","系统通知","subject","系统通知","content","您好 {username}，\n\n{message}\n\n此邮件由系统自动发送，请勿回复。")
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

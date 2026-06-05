package com.yunxingcloud.yunxingcloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@yunxingcloud.com}")
    private String from;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = baseUrl + "/#/reset-password?token=" + token;
        String body = "您好，\n\n您正在申请重置 yunxingcloud 账号密码。\n\n" +
                "重置链接: " + resetUrl + "\n\n" +
                "该链接 24 小时内有效。如非本人操作，请忽略此邮件。\n\n" +
                "yunxingcloud 团队";

        if (mailSender == null) {
            log.info("[DEV] 模拟邮件发送至 {}: 重置链接={}", toEmail, resetUrl);
            throw new RuntimeException("邮件服务未配置（开发模式）");
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(toEmail);
            msg.setSubject("yunxingcloud - 密码重置");
            msg.setText(body);
            mailSender.send(msg);
            log.info("密码重置邮件已发送至: {}", toEmail);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }
}

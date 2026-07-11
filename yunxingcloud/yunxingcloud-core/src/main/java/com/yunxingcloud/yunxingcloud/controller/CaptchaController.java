package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.CaptchaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "验证码", description = "图形验证码生成与RSA公钥")
@RestController
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/api/captcha")
    public ResponseEntity<Map<String, Object>> captcha(HttpSession session) {
        return ResponseEntity.ok(captchaService.generateCaptcha(session));
    }

    @GetMapping("/api/publicKey")
    public ResponseEntity<Map<String, String>> publicKey() {
        return ResponseEntity.ok(captchaService.getPublicKey());
    }
}

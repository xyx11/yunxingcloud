package com.yunxingcloud.payment.controller;

import com.yunxingcloud.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/callback")
public class PaymentCallbackController {

    private final PaymentService paymentService;

    public PaymentCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/wechat")
    public ResponseEntity<?> wechatCallback(HttpServletRequest request, @RequestBody(required = false) String body) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        paymentService.handleCallback("wechat", params, body != null ? body : "");
        return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "OK"));
    }

    @PostMapping("/alipay")
    public ResponseEntity<?> alipayCallback(HttpServletRequest request, @RequestBody(required = false) String body) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        paymentService.handleCallback("alipay", params, body != null ? body : "");
        return ResponseEntity.ok("success");
    }
}

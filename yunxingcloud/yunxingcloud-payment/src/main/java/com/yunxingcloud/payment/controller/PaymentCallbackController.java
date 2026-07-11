package com.yunxingcloud.payment.controller;

import com.yunxingcloud.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "支付回调", description = "微信/支付宝支付回调")
@RestController
@RequestMapping("/api/payment/callback")
public class PaymentCallbackController {

    private final PaymentService paymentService;

    public PaymentCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "微信支付回调")
    @PostMapping("/wechat")
    public ResponseEntity<?> wechatCallback(HttpServletRequest request, @RequestBody(required = false) String body) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        paymentService.handleCallback("wechat", params, body != null ? body : "");
        return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "OK"));
    }

    @Operation(summary = "支付宝回调")
    @PostMapping("/alipay")
    public ResponseEntity<?> alipayCallback(HttpServletRequest request, @RequestBody(required = false) String body) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        paymentService.handleCallback("alipay", params, body != null ? body : "");
        return ResponseEntity.ok("success");
    }
}

package com.yunxingcloud.payment.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.payment.entity.PaymentOrder;
import com.yunxingcloud.payment.repository.PaymentOrderRepository;
import com.yunxingcloud.payment.service.PaymentService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "支付管理", description = "支付创建与查询")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentOrderRepository orderRepo;
    private final PaymentService paymentService;

    public PaymentController(PaymentOrderRepository orderRepo, PaymentService paymentService) {
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @GetMapping("/orders")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(orderRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "查询支付状态")
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return orderRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "支付管理", businessType = BusinessType.INSERT)
    @Operation(summary = "创建支付")
    @PostMapping("/orders")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        Long amount = Long.valueOf(body.get("amount").toString());
        String channel = (String) body.get("channel");
        log.info("创建支付订单: title={}, amount={}, channel={}", title, amount, channel);
        return ResponseEntity.ok(paymentService.create(title, amount, channel));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String baseUrl = body != null ? body.getOrDefault("notifyUrl", "https://www.yunxingcloud.com") : "https://www.yunxingcloud.com";
        return ResponseEntity.ok(paymentService.pay(id, baseUrl));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Operation(summary = "申请退款")
    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<?> refund(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long refundAmount = Long.valueOf(body.get("refundAmount").toString());
        String reason = (String) body.getOrDefault("reason", "");
        log.info("申请退款: orderId={}, refundAmount={}, reason={}", id, refundAmount, reason);
        try {
            return ResponseEntity.ok(paymentService.refund(id, refundAmount, reason));
        } catch (Exception e) {
            log.warn("退款操作失败: orderId={}, amount={}, reason={}, error={}", id, refundAmount, reason, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @GetMapping("/orders/{id}/records")
    public ResponseEntity<?> records(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.records(id));
    }
}

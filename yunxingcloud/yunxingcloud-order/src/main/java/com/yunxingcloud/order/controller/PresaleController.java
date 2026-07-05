package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.PresaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "预售管理", description = "商品预售定金/尾款支付")
@RestController
@RequestMapping("/api/presale")
public class PresaleController {

    private final PresaleService presaleService;

    public PresaleController(PresaleService presaleService) { this.presaleService = presaleService; }

    private String user() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new SecurityException("未登录");
        return auth.getName();
    }

    @Operation(summary = "预售活动列表")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(presaleService.list(page, size));
    }

    @Operation(summary = "预售活动详情")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@Parameter(description = "预售活动ID") @PathVariable Long id) {
        return presaleService.detail(id)
                .<ResponseEntity<Map<String, Object>>>map(p -> ResponseEntity.ok(Map.of("presale", p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "支付定金")
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@Parameter(description = "预售活动ID") @PathVariable Long id) {
        try {
            var order = presaleService.deposit(id, user());
            return ResponseEntity.ok(Map.of("success", true, "orderId", order.getId(), "orderNo", order.getOrderNo()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "支付尾款")
    @PostMapping("/{id}/final-pay")
    public ResponseEntity<Map<String, Object>> finalPay(@Parameter(description = "预售活动ID") @PathVariable Long id,
                                                         @Parameter(description = "定金订单ID") @RequestParam Long orderId) {
        try {
            var result = presaleService.finalPay(id, orderId, user());
            return ResponseEntity.ok(Map.of("success", true, "orderId", result.get("orderId"), "finalAmount", result.get("finalAmount")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}
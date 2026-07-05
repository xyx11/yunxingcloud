




package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.PresaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(presaleService.list(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return presaleService.detail(id)
                .<ResponseEntity<Map<String, Object>>>map(p -> ResponseEntity.ok(Map.of("presale", p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@PathVariable Long id) {
        try {
            var order = presaleService.deposit(id, user());
            return ResponseEntity.ok(Map.of("success", true, "orderId", order.getId(), "orderNo", order.getOrderNo()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/final-pay")
    public ResponseEntity<Map<String, Object>> finalPay(@PathVariable Long id, @RequestParam Long orderId) {
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

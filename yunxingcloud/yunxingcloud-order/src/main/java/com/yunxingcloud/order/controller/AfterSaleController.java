package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.AfterSale;
import com.yunxingcloud.order.repository.AfterSaleRepository;
import com.yunxingcloud.order.service.AfterSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "售后管理", description = "退换货/售后工单处理")
@RestController
@RequestMapping("/api/after-sale")
public class AfterSaleController {

    private static final Logger log = LoggerFactory.getLogger(AfterSaleController.class);

    private final AfterSaleRepository afterSaleRepo;
    private final AfterSaleService service;

    public AfterSaleController(AfterSaleRepository afterSaleRepo, AfterSaleService service) {
        this.afterSaleRepo = afterSaleRepo;
        this.service = service;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> "admin".equals(a.getAuthority())); }

    @GetMapping
    public ResponseEntity<?> list() {
        if (isAdmin()) return ResponseEntity.ok(afterSaleRepo.findAll());
        return ResponseEntity.ok(afterSaleRepo.findByUsernameOrderByCreatedAtDesc(user()));
    }

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody Map<String, Object> body) {
        if (body.get("orderId") == null || body.get("type") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "orderId and type are required"));
        }
        AfterSale as = service.apply(
                Long.valueOf(body.get("orderId").toString()), user(),
                (String) body.get("type"), (String) body.get("reason"),
                body.containsKey("refundAmount") ? Long.valueOf(body.get("refundAmount").toString()) : null,
                (String) body.getOrDefault("evidenceUrls", ""));
        log.info("User {} applied after-sale for order {}, type={}", user(),
                body.get("orderId"), body.get("type"));
        return ResponseEntity.ok(as);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        var result = service.approve(id, body != null ? body.getOrDefault("remark", "") : "");
        log.info("Admin approved after-sale {}, remark={}", id, body != null ? body.get("remark") : "");
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var result = service.reject(id, body.getOrDefault("remark", ""));
        log.info("Admin rejected after-sale {}, remark={}", id, body.get("remark"));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        AfterSale as = afterSaleRepo.findById(id).orElse(null);
        if (as == null) return ResponseEntity.notFound().build();
        if (!as.getUsername().equals(user())) return ResponseEntity.status(403).body(Map.of("message", "无权操作"));
        if (!"0".equals(as.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "仅待审核的售后单可撤销"));
        afterSaleRepo.delete(as);
        log.info("User {} cancelled after-sale {}", user(), id);
        return ResponseEntity.ok(Map.of("message", "已撤销"));
    }
}
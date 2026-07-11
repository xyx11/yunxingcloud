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

import java.util.Map;

@Tag(name = "售后管理", description = "退换货/售后工单处理")
@RestController
@RequestMapping("/api/after-sale")
public class AfterSaleController {

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
        AfterSale as = service.apply(
                Long.valueOf(body.get("orderId").toString()), user(),
                (String) body.get("type"), (String) body.get("reason"),
                body.containsKey("refundAmount") ? Long.valueOf(body.get("refundAmount").toString()) : null,
                (String) body.getOrDefault("evidenceUrls", ""));
        return ResponseEntity.ok(as);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(service.approve(id, body != null ? body.getOrDefault("remark", "") : ""));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.reject(id, body.getOrDefault("remark", "")));
    }
}
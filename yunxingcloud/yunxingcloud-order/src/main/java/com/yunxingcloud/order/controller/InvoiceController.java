package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Invoice;
import com.yunxingcloud.order.repository.InvoiceRepository;
import com.yunxingcloud.order.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "发票管理", description = "发票申请与查询")
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceRepository invoiceRepo;
    private final InvoiceService service;
    private final com.yunxingcloud.order.repository.OrderHeadRepository orderRepo;

    public InvoiceController(InvoiceRepository invoiceRepo, InvoiceService service,
                             com.yunxingcloud.order.repository.OrderHeadRepository orderRepo) {
        this.invoiceRepo = invoiceRepo;
        this.service = service;
        this.orderRepo = orderRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> "admin".equals(a.getAuthority())); }

    @GetMapping
    public ResponseEntity<?> list() {
        if (isAdmin()) return ResponseEntity.ok(invoiceRepo.findAll());
        return ResponseEntity.ok(invoiceRepo.findByUsernameOrderByCreatedAtDesc(user()));
    }

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody Map<String, Object> body) {
        Object orderIdObj = body.get("orderId");
        if (orderIdObj == null) {
            Object orderNoObj = body.get("orderNo");
            if (orderNoObj == null)
                return ResponseEntity.badRequest().body(Map.of("message", "缺少订单ID或订单号"));
            var order = orderRepo.findByOrderNo(orderNoObj.toString()).orElse(null);
            if (order == null) return ResponseEntity.badRequest().body(Map.of("message", "订单不存在"));
            orderIdObj = order.getId();
        }
        Invoice inv = service.apply(
                Long.valueOf(orderIdObj.toString()), user(),
                (String) body.get("type"), (String) body.getOrDefault("title", ""),
                (String) body.getOrDefault("taxNo", ""), (String) body.getOrDefault("email", ""));
        log.info("User {} applied invoice for orderId={}, type={}", user(), orderIdObj, body.get("type"));
        return ResponseEntity.ok(inv);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/issue")
    public ResponseEntity<?> issue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var result = service.issue(id, body.get("invoiceNo"), body.get("invoiceUrl"));
        log.info("Admin issued invoice {}, invoiceNo={}", id, body.get("invoiceNo"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() { return ResponseEntity.ok(java.util.Map.of("endpoints", 4)); }
}
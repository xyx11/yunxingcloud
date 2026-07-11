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

import java.util.Map;

@Tag(name = "发票管理", description = "发票申请与查询")
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceService service;

    public InvoiceController(InvoiceRepository invoiceRepo, InvoiceService service) {
        this.invoiceRepo = invoiceRepo;
        this.service = service;
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
        Invoice inv = service.apply(
                Long.valueOf(body.get("orderId").toString()), user(),
                (String) body.get("type"), (String) body.getOrDefault("title", ""),
                (String) body.getOrDefault("taxNo", ""), (String) body.getOrDefault("email", ""));
        return ResponseEntity.ok(inv);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/issue")
    public ResponseEntity<?> issue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.issue(id,
                body.get("invoiceNo"), body.get("invoiceUrl")));
    }
}
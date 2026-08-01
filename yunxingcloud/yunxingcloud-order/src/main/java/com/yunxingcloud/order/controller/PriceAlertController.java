package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.PriceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "价格提醒", description = "商品降价提醒")
@RestController
@RequestMapping("/api/price-alert")
public class PriceAlertController {

    private final PriceAlertService service;

    public PriceAlertController(PriceAlertService service) { this.service = service; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(service.create(user(), body.get("productId"), body.get("targetPrice")));
    }

    @GetMapping("/check")
    public ResponseEntity<?> check() { return ResponseEntity.ok(service.checkAndNotify(user())); }

    @GetMapping("/list")
    public ResponseEntity<?> list() { return ResponseEntity.ok(service.list(user())); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) { service.remove(id, user()); return ResponseEntity.ok().build(); }

    @GetMapping("/admin/list")
    public ResponseEntity<?> adminList() { return ResponseEntity.ok(service.listAll()); }
}
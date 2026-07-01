package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
public class DataExportController {

    private final RestTemplate rest;

    public DataExportController(RestTemplate rest) {
        this.rest = rest;
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @GetMapping("/products")
    public ResponseEntity<byte[]> exportProducts() {
        byte[] data = rest.getForObject("http://yunxingcloud-order/api/products/export", byte[].class);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header("Content-Disposition", "attachment; filename=products.csv")
                .body(data != null ? data : "".getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/all")
    public ResponseEntity<?> exportAll() {
        return ResponseEntity.ok(java.util.Map.of(
                "products", "/api/export/products",
                "orders", "/api/orders (GET)",
                "tickets", "/api/tickets (GET)",
                "message", "各端点支持直接GET导出"
        ));
    }
}
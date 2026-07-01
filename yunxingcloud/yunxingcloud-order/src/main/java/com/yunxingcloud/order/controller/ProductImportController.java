package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ProductImportService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductImportController {

    private final ProductImportService service;

    public ProductImportController(ProductImportService service) { this.service = service; }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.importExcel(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ticket:write')")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = service.exportExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition", "attachment; filename=products.xlsx")
                .body(data);
    }
}
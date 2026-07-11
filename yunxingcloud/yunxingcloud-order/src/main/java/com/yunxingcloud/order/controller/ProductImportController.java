package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ProductImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "商品导入", description = "商品批量导入导出")
@RestController
@RequestMapping("/api/products")
public class ProductImportController {

    private final ProductImportService service;

    public ProductImportController(ProductImportService service) { this.service = service; }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/import")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.importCsv(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ticket:write')")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] data = service.exportCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header("Content-Disposition", "attachment; filename=products.csv")
                .body(data);
    }
}
package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.ProductBundleDTO;
import com.yunxingcloud.order.service.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "捆绑销售", description = "商品捆绑套餐管理")
@RestController
@RequestMapping("/api/bundles")
public class BundleController {

    private final BundleService bundleService;

    public BundleController(BundleService bundleService) { this.bundleService = bundleService; }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(bundleService.list()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        Map<String, Object> result = bundleService.detail(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductBundleDTO dto) {
        return ResponseEntity.ok(bundleService.create(dto));
    }
}

package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.ProductBundleDTO;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bundles")
public class BundleController {

    private final ProductBundleRepository bundleRepo;
    private final ProductRepository productRepo;

    public BundleController(ProductBundleRepository bundleRepo, ProductRepository productRepo) {
        this.bundleRepo = bundleRepo; this.productRepo = productRepo;
    }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(bundleRepo.findByStatus("1")); }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        ProductBundle b = bundleRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        List<Product> items = new ArrayList<>();
        for (String pid : b.getProductIds().split(",")) {
            productRepo.findById(Long.parseLong(pid.trim())).ifPresent(items::add);
        }
        return ResponseEntity.ok(Map.of("bundle", b, "items", items,
                "savings", (b.getOriginalPrice() != null ? b.getOriginalPrice() : 0) - b.getBundlePrice()));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductBundleDTO dto) {
        ProductBundle b = new ProductBundle();
        b.setName(dto.getName()); b.setBundlePrice(dto.getBundlePrice()); b.setOriginalPrice(dto.getOriginalPrice());
        b.setProductIds(dto.getProductIds()); b.setImageUrl(dto.getImageUrl()); b.setStatus(dto.getStatus());
        return ResponseEntity.ok(bundleRepo.save(b));
    }
}
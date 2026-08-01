package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Supplier;
import com.yunxingcloud.order.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理", description = "供应商信息管理")
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) { this.supplierService = supplierService; }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(supplierService.list()); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Supplier s) { return ResponseEntity.ok(supplierService.create(s)); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Supplier s) { return ResponseEntity.ok(supplierService.update(id, s)); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return supplierService.get(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() { return ResponseEntity.ok(java.util.Map.of("count", supplierService.list().size())); }
}

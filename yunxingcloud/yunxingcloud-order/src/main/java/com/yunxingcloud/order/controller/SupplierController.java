package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Supplier;
import com.yunxingcloud.order.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}

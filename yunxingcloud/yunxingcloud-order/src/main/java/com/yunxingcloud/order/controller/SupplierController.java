package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Supplier;
import com.yunxingcloud.order.repository.SupplierRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierRepository repo;

    public SupplierController(SupplierRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(repo.findByStatus("1")); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Supplier s) { return ResponseEntity.ok(repo.save(s)); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Supplier s) { s.setId(id); return ResponseEntity.ok(repo.save(s)); }
}
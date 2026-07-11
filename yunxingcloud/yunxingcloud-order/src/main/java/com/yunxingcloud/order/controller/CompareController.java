package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.CompareList;
import com.yunxingcloud.order.service.CompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商品对比", description = "商品对比功能")
@RestController
@RequestMapping("/api/compare")
public class CompareController {

    private final CompareService compareService;

    public CompareController(CompareService compareService) { this.compareService = compareService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(compareService.list(user()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> add(@PathVariable Long productId) {
        try {
            CompareList c = compareService.add(user(), productId);
            if (c == null) return ResponseEntity.ok(Map.of("message", "已存在"));
            return ResponseEntity.ok(c);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remove(@PathVariable Long productId) {
        compareService.remove(user(), productId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        compareService.clear(user());
        return ResponseEntity.ok(Map.of("success", true));
    }
}

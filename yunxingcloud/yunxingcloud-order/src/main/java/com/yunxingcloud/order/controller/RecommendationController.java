package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品推荐", description = "商品推荐引擎")
@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) { this.service = service; }

    @GetMapping("/also-bought/{productId}")
    public ResponseEntity<?> alsoBought(@PathVariable Long productId, @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(service.alsoBought(productId, limit));
    }

    @GetMapping("/for-you")
    public ResponseEntity<?> forYou(@RequestParam(defaultValue = "8") int limit) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.forUser(username, limit));
    }

    @GetMapping("/hot")
    public ResponseEntity<?> hot(@RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(service.hotProducts(limit));
    }

    @GetMapping("/new")
    public ResponseEntity<?> newArrivals(@RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(service.newArrivals(limit));
    }
}
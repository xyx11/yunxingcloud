package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "购物车", description = "购物车管理")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(cartService.list(user()));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, Object> body) {
        Object pid = body.get("productId");
        if (pid == null) return ResponseEntity.badRequest().body(Map.of("message", "缺少商品ID"));
        try {
            var result = cartService.add(user(), Long.valueOf(pid.toString()),
                    body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1);
            log.info("User {} added product {} qty {} to cart", user(), pid,
                    body.getOrDefault("quantity", "1"));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("User {} failed to add product {}: {}", user(), pid, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        cartService.remove(id, user());
        log.info("User {} removed cart item {}", user(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        cartService.clear(user());
        log.info("User {} cleared cart", user());
        return ResponseEntity.ok().build();
    }
}

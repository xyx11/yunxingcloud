package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.CartItem;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CartItemRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;

    public CartController(CartItemRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() {
        var items = cartRepo.findByUsernameOrderByCreatedAtDesc(user());
        if (items.isEmpty()) {
            var recs = productRepo.findByStatus("0", PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "sales")));
            return ResponseEntity.ok(Map.of("items", items, "recommended", recs));
        }
        return ResponseEntity.ok(Map.of("items", items));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, Object> body) {
        Object pid = body.get("productId");
        if (pid == null) return ResponseEntity.badRequest().body(Map.of("message", "缺少商品ID"));
        Long productId = Long.valueOf(pid.toString());
        int quantity = body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1;
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return ResponseEntity.badRequest().body(Map.of("message", "商品不存在"));
        if (p.getStock() < quantity) return ResponseEntity.badRequest().body(Map.of("message", "库存不足"));
        CartItem item = new CartItem();
        item.setUsername(user());
        item.setProductId(productId);
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setQuantity(quantity);
        return ResponseEntity.ok(cartRepo.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        cartRepo.findById(id).ifPresent(c -> {
            if (c.getUsername().equals(user())) cartRepo.delete(c);
        });
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        cartRepo.deleteByUsername(user());
        return ResponseEntity.ok().build();
    }
}

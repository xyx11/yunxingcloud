package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.CompareList;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CompareListRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/compare")
public class CompareController {

    private final CompareListRepository compareRepo;
    private final ProductRepository productRepo;

    public CompareController(CompareListRepository compareRepo, ProductRepository productRepo) {
        this.compareRepo = compareRepo; this.productRepo = productRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() {
        List<CompareList> items = compareRepo.findByUsernameOrderByCreatedAtDesc(user());
        List<Product> products = new ArrayList<>();
        for (CompareList c : items) {
            productRepo.findById(c.getProductId()).ifPresent(products::add);
        }
        // 提取对比属性
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            result.add(Map.of("id", p.getId(), "name", p.getName(),
                    "price", p.getPrice(), "stock", p.getStock(),
                    "sales", p.getSales() != null ? p.getSales() : 0,
                    "description", p.getDescription() != null ? p.getDescription() : ""));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> add(@PathVariable Long productId) {
        List<CompareList> existing = compareRepo.findByUsernameOrderByCreatedAtDesc(user());
        if (existing.size() >= 4) throw new IllegalStateException("最多对比4个商品");
        if (existing.stream().anyMatch(c -> c.getProductId().equals(productId)))
            return ResponseEntity.ok(Map.of("message", "已存在"));
        CompareList c = new CompareList(); c.setUsername(user()); c.setProductId(productId);
        return ResponseEntity.ok(compareRepo.save(c));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remove(@PathVariable Long productId) {
        compareRepo.deleteByUsernameAndProductId(user(), productId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        compareRepo.deleteByUsername(user());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
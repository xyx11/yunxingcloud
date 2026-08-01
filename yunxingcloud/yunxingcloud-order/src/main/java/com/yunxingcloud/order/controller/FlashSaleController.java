package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.FlashSale;
import com.yunxingcloud.order.repository.FlashSaleRepository;
import com.yunxingcloud.order.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "秒杀管理", description = "限时秒杀活动")
@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

    private static final Logger log = LoggerFactory.getLogger(FlashSaleController.class);

    private final FlashSaleRepository flashRepo;
    private final FlashSaleService service;

    public FlashSaleController(FlashSaleRepository flashRepo, FlashSaleService service) {
        this.flashRepo = flashRepo;
        this.service = service;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(flashRepo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        FlashSale fs = flashRepo.findById(id).orElse(null);
        if (fs == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("flashSale", fs, "remainingStock", service.getRemainingStock(id)));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody FlashSale fs) {
        var saved = flashRepo.save(fs);
        log.info("Flash sale created: id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/{id}/preheat")
    public ResponseEntity<?> preheat(@PathVariable Long id) {
        service.preheat(id);
        long stock = service.getRemainingStock(id);
        log.info("Flash sale {} preheated, remaining stock={}", id, stock);
        return ResponseEntity.ok(Map.of("success", true, "stock", stock));
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buy(@PathVariable Long id, @RequestParam Long productId) {
        try {
            service.tryBuy(id, productId, user());
            log.info("User {} bought flash sale {}, productId={}", user(), id, productId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalStateException e) {
            log.warn("User {} failed to buy flash sale {}: {}", user(), id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        service.confirmOrder(id, user());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id) {
        service.releaseReservation(id, user());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/remind")
    public ResponseEntity<?> remind(@RequestBody Map<String, Object> body) {
        Long saleId = Long.valueOf(body.get("saleId").toString());
        Long productId = body.containsKey("productId") ? Long.valueOf(body.get("productId").toString()) : null;
        service.setRemind(saleId, productId, user());
        log.info("User {} set remind for flash sale {}", user(), saleId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
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

import java.util.Map;

@Tag(name = "秒杀管理", description = "限时秒杀活动")
@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

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
    public ResponseEntity<?> create(@RequestBody FlashSale fs) { return ResponseEntity.ok(flashRepo.save(fs)); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/{id}/preheat")
    public ResponseEntity<?> preheat(@PathVariable Long id) {
        service.preheat(id);
        return ResponseEntity.ok(Map.of("success", true, "stock", service.getRemainingStock(id)));
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buy(@PathVariable Long id, @RequestParam Long productId) {
        try {
            service.tryBuy(id, productId, user());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
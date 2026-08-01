package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Campaign;
import com.yunxingcloud.order.repository.CampaignRepository;
import com.yunxingcloud.order.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "营销活动", description = "促销活动管理")
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private static final Logger log = LoggerFactory.getLogger(CampaignController.class);

    private final CampaignRepository repo;
    private final CampaignService service;

    public CampaignController(CampaignRepository repo, CampaignService service) {
        this.repo = repo; this.service = service;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(service.getActiveCampaigns()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return service.get(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Campaign c) {
        var saved = service.create(c);
        log.info("Campaign created: id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Campaign c) {
        c.setId(id);
        return ResponseEntity.ok(service.create(c));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/calculate")
    public ResponseEntity<?> calculate(@PathVariable Long id, @RequestParam Long amount) {
        try {
            long discount = service.calculateDiscount(id, amount, user());
            log.info("User {} calculated campaign {} discount: amount={}, discount={}", user(), id, amount, discount);
            return ResponseEntity.ok(Map.of("discount", discount, "finalAmount", amount - discount));
        } catch (Exception e) {
            log.warn("User {} failed to calculate campaign {} discount: {}", user(), id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> campaignProducts(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCampaignProducts(id));
    }

    @GetMapping("/active")
    public ResponseEntity<?> activeCampaigns() {
        return ResponseEntity.ok(service.getActiveCampaigns());
    }
}
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
    public ResponseEntity<?> active() {
        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok(repo.findByStatusAndStartTimeBeforeAndEndTimeAfter("1", now, now));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Campaign c) {
        var saved = repo.save(c);
        log.info("Campaign created: id={}", saved.getId());
        return ResponseEntity.ok(saved);
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
}
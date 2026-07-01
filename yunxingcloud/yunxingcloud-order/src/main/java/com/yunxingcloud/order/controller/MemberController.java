package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.MemberTier;
import com.yunxingcloud.order.repository.MemberTierRepository;
import com.yunxingcloud.order.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService service;
    private final MemberTierRepository tierRepo;

    public MemberController(MemberService service, MemberTierRepository tierRepo) {
        this.service = service; this.tierRepo = tierRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/benefits")
    public ResponseEntity<?> myBenefits() { return ResponseEntity.ok(service.benefits(user())); }

    @GetMapping("/tiers")
    public ResponseEntity<?> tiers() { return ResponseEntity.ok(tierRepo.findAllByOrderByMinPointsAsc()); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/tiers")
    public ResponseEntity<?> createTier(@RequestBody MemberTier tier) { return ResponseEntity.ok(tierRepo.save(tier)); }
}
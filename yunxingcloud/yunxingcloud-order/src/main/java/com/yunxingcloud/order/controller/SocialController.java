package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final WishlistRepository wishRepo;
    private final ShareRecordRepository shareRepo;

    public SocialController(WishlistRepository wishRepo, ShareRecordRepository shareRepo) {
        this.wishRepo = wishRepo; this.shareRepo = shareRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    // ---- 心愿单 ----
    @GetMapping("/wishlist")
    public ResponseEntity<?> wishlist() { return ResponseEntity.ok(wishRepo.findByUsername(user())); }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<?> addWish(@PathVariable Long productId) {
        if (wishRepo.existsByUsernameAndProductId(user(), productId))
            return ResponseEntity.ok(Map.of("message", "已存在"));
        Wishlist w = new Wishlist(); w.setUsername(user()); w.setProductId(productId);
        return ResponseEntity.ok(wishRepo.save(w));
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<?> removeWish(@PathVariable Long productId) {
        wishRepo.deleteByUsernameAndProductId(user(), productId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ---- 分享 ----
    @PostMapping("/share")
    public ResponseEntity<?> share(@RequestBody Map<String, Object> body) {
        ShareRecord sr = new ShareRecord();
        sr.setSharer(user());
        sr.setProductId(Long.valueOf(body.get("productId").toString()));
        sr.setChannel((String) body.getOrDefault("channel", "copy"));
        return ResponseEntity.ok(shareRepo.save(sr));
    }

    @PostMapping("/share/{id}/click")
    public ResponseEntity<?> click(@PathVariable Long id) {
        shareRepo.findById(id).ifPresent(s -> { s.setClickCount(s.getClickCount() + 1); shareRepo.save(s); });
        return ResponseEntity.ok(Map.of("success", true));
    }
}
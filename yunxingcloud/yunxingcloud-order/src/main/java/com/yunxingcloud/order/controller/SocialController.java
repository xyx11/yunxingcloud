package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.ShareRecord;
import com.yunxingcloud.order.entity.Wishlist;
import com.yunxingcloud.order.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "社交功能", description = "社交分享与互动")
@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) { this.socialService = socialService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/wishlist")
    public ResponseEntity<?> wishlist() { return ResponseEntity.ok(socialService.wishlist(user())); }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<?> addWish(@PathVariable Long productId) {
        Wishlist w = socialService.addWish(user(), productId);
        if (w == null) return ResponseEntity.ok(Map.of("message", "已存在"));
        return ResponseEntity.ok(w);
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<?> removeWish(@PathVariable Long productId) {
        socialService.removeWish(user(), productId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/share")
    public ResponseEntity<?> share(@RequestBody Map<String, Object> body) {
        ShareRecord sr = socialService.share(user(),
                Long.valueOf(body.get("productId").toString()),
                (String) body.getOrDefault("channel", "copy"));
        return ResponseEntity.ok(sr);
    }

    @PostMapping("/share/{id}/click")
    public ResponseEntity<?> click(@PathVariable Long id) {
        socialService.click(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

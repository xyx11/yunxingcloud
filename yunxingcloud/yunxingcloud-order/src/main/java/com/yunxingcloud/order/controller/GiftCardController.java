package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.GiftCard;
import com.yunxingcloud.order.service.GiftCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "礼品卡", description = "礼品卡管理")
@RestController
@RequestMapping("/api/gift-cards")
public class GiftCardController {

    private final GiftCardService service;

    public GiftCardController(GiftCardService service) { this.service = service; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/{cardNo}")
    public ResponseEntity<?> query(@PathVariable String cardNo) {
        GiftCard card = service.query(cardNo);
        return card != null ? ResponseEntity.ok(card) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{cardNo}/activate")
    public ResponseEntity<?> activate(@PathVariable String cardNo) {
        try {
            return ResponseEntity.ok(service.activate(cardNo, user()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{cardNo}/pay")
    public ResponseEntity<?> pay(@PathVariable String cardNo, @RequestBody Map<String, Long> body) {
        try {
            long deducted = service.pay(cardNo, body.get("amount"));
            return ResponseEntity.ok(Map.of("success", true, "deducted", deducted));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long amount = Long.valueOf(body.get("amount").toString());
        int expireDays = body.containsKey("expireDays") ? Integer.parseInt(body.get("expireDays").toString()) : 365;
        return ResponseEntity.ok(service.create(amount, expireDays));
    }
}
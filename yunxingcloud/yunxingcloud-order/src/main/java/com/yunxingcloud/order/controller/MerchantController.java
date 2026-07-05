package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Merchant;
import com.yunxingcloud.order.service.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) { this.merchantService = merchantService; }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody Merchant merchant) {
        try {
            Merchant m = merchantService.apply(merchant);
            return ResponseEntity.ok(Map.of("success", true, "merchantId", m.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/status/{phone}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String phone) {
        return merchantService.status(phone)
                .map(m -> ResponseEntity.ok(Map.of("merchant", (Object) m)))
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "未找到申请记录")));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") String status) {
        return ResponseEntity.ok(Map.of("merchants", merchantService.list(status)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id) {
        try {
            Merchant m = merchantService.approve(id);
            return ResponseEntity.ok(Map.of("success", true, "merchantId", id, "status", m.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id) {
        try {
            Merchant m = merchantService.reject(id);
            return ResponseEntity.ok(Map.of("success", true, "merchantId", id, "status", m.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

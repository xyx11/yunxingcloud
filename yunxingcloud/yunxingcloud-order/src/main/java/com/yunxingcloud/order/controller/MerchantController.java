package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Merchant;
import com.yunxingcloud.order.repository.MerchantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantRepository merchantRepo;

    public MerchantController(MerchantRepository merchantRepo) {
        this.merchantRepo = merchantRepo;
    }

    /** 商家入驻申请 */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody Merchant merchant) {
        Merchant existing = merchantRepo.findByPhone(merchant.getPhone());
        if (existing != null) return ResponseEntity.badRequest().body(Map.of("message", "该手机号已提交过申请"));

        merchant.setStatus("0");
        merchantRepo.save(merchant);
        return ResponseEntity.ok(Map.of("success", true, "merchantId", merchant.getId()));
    }

    /** 查询申请状态 */
    @GetMapping("/status/{phone}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String phone) {
        Merchant merchant = merchantRepo.findByPhone(phone);
        if (merchant == null) return ResponseEntity.badRequest().body(Map.of("message", "未找到申请记录"));
        return ResponseEntity.ok(Map.of("merchant", merchant));
    }

    /** 管理员审核列表 */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") String status) {
        List<Merchant> list = merchantRepo.findByStatus(status);
        return ResponseEntity.ok(Map.of("merchants", list));
    }

    /** 审核通过 */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id) {
        Merchant merchant = merchantRepo.findById(id).orElse(null);
        if (merchant == null) return ResponseEntity.badRequest().body(Map.of("message", "商家不存在"));
        if (!"0".equals(merchant.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "当前状态不可审核"));

        merchant.setStatus("1");
        merchantRepo.save(merchant);
        return ResponseEntity.ok(Map.of("success", true, "merchantId", id, "status", "1"));
    }

    /** 审核拒绝 */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id) {
        Merchant merchant = merchantRepo.findById(id).orElse(null);
        if (merchant == null) return ResponseEntity.badRequest().body(Map.of("message", "商家不存在"));
        if (!"0".equals(merchant.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "当前状态不可审核"));

        merchant.setStatus("2");
        merchantRepo.save(merchant);
        return ResponseEntity.ok(Map.of("success", true, "merchantId", id, "status", "2"));
    }
}

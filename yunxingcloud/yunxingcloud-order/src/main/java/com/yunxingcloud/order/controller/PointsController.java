package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.PointsAccount;
import com.yunxingcloud.order.entity.PointsRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.yunxingcloud.order.repository.PointsAccountRepository;
import com.yunxingcloud.order.repository.PointsRecordRepository;
import com.yunxingcloud.order.service.PointsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "积分管理", description = "用户积分管理")
@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService service;
    private final PointsAccountRepository accountRepo;
    private final PointsRecordRepository recordRepo;

    public PointsController(PointsService service, PointsAccountRepository accountRepo,
                            PointsRecordRepository recordRepo) {
        this.service = service; this.accountRepo = accountRepo; this.recordRepo = recordRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/account")
    public ResponseEntity<?> myAccount() {
        PointsAccount acc = service.getAccount(user());
        return ResponseEntity.ok(acc != null ? acc : Map.of("balance", 0));
    }

    @GetMapping("/records")
    public ResponseEntity<?> myRecords() {
        return ResponseEntity.ok(recordRepo.findByUsernameOrderByCreatedAtDesc(user()));
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestBody Map<String, String> body) {
        if (!body.containsKey("points") || !body.containsKey("orderId"))
            return ResponseEntity.badRequest().body(Map.of("message", "缺少参数"));
        Long points = Long.valueOf(body.get("points"));
        Long orderId = Long.valueOf(body.get("orderId"));
        long deductedCents = service.redeem(user(), points, orderId);
        return ResponseEntity.ok(Map.of("success", true, "deductedCents", deductedCents));
    }

    @GetMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestParam Long points) {
        PointsAccount acc = service.getAccount(user());
        long availablePoints = acc != null ? acc.getBalance() : 0;
        long usable = Math.min(points, availablePoints);
        long deductedCents = usable / 100 * 100; // 100积分=1元
        return ResponseEntity.ok(Map.of(
            "availablePoints", availablePoints,
            "usable", usable,
            "deductedCents", deductedCents
        ));
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin() {
        try {
            service.checkin(user());
            PointsAccount acc = service.getAccount(user());
            return ResponseEntity.ok(Map.of("success", true, "balance", acc != null ? acc.getBalance() : 0));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/checkin/status")
    public ResponseEntity<?> checkinStatus() {
        boolean checked = service.hasCheckedInToday(user());
        return ResponseEntity.ok(Map.of("checked", checked));
    }

    @GetMapping("/exchanges")
    public ResponseEntity<?> exchangeItems() {
        return ResponseEntity.ok(java.util.List.of(
            Map.of("id", 1, "name", "50元优惠券", "points", 5000, "stock", 100),
            Map.of("id", 2, "name", "10元话费", "points", 1000, "stock", 200),
            Map.of("id", 3, "name", "包邮券", "points", 500, "stock", 500),
            Map.of("id", 4, "name", "限定周边", "points", 3000, "stock", 50)
        ));
    }

    @PostMapping("/exchange/{itemId}")
    public ResponseEntity<?> doExchange(@PathVariable Long itemId) {
        PointsAccount acc = service.getAccount(user());
        long balance = acc != null ? acc.getBalance() : 0;
        // Simple check: each item costs based on id
        long[] costs = {0, 5000, 1000, 500, 3000};
        int idx = itemId.intValue();
        if (idx < 1 || idx >= costs.length)
            return ResponseEntity.badRequest().body(Map.of("message", "无效的商品"));
        long cost = costs[idx];
        if (balance < cost)
            return ResponseEntity.badRequest().body(Map.of("message", "积分不足"));
        service.redeem(user(), cost, 0L);
        return ResponseEntity.ok(Map.of("success", true, "message", "兑换成功"));
    }
}
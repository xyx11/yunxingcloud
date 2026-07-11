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
        Long points = Long.valueOf(body.get("points"));
        Long orderId = Long.valueOf(body.get("orderId"));
        long deductedCents = service.redeem(user(), points, orderId);
        return ResponseEntity.ok(Map.of("success", true, "deductedCents", deductedCents));
    }
}
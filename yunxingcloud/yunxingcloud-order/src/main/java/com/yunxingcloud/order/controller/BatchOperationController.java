package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.BatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "批量操作", description = "批量处理工具")
@RestController
@RequestMapping("/api/batch")
public class BatchOperationController {

    private final BatchOperationService batchOperationService;

    public BatchOperationController(BatchOperationService batchOperationService) { this.batchOperationService = batchOperationService; }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/ship")
    public ResponseEntity<?> batchShip(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String carrier = (String) body.getOrDefault("carrier", "");
        return ResponseEntity.ok(batchOperationService.batchShip(ids, carrier));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/cancel")
    public ResponseEntity<?> batchCancel(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String reason = (String) body.getOrDefault("reason", "批量取消");
        return ResponseEntity.ok(batchOperationService.batchCancel(ids, reason));
    }
}

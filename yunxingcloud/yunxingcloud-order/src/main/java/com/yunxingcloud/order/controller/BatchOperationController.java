package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/batch")
public class BatchOperationController {

    private final OrderHeadRepository orderRepo;

    public BatchOperationController(OrderHeadRepository orderRepo) { this.orderRepo = orderRepo; }

    /** 批量发货 */
    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/ship")
    public ResponseEntity<?> batchShip(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String carrier = (String) body.getOrDefault("carrier", "");
        int count = 0;
        for (Integer id : ids) {
            OrderHead order = orderRepo.findById(Long.valueOf(id)).orElse(null);
            if (order != null && "1".equals(order.getStatus())) {
                order.setStatus("2");
                order.setRemark("批量发货: " + carrier);
                orderRepo.save(order);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of("success", true, "shipped", count, "total", ids.size()));
    }

    /** 批量取消 */
    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/cancel")
    public ResponseEntity<?> batchCancel(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String reason = (String) body.getOrDefault("reason", "批量取消");
        int count = 0;
        for (Integer id : ids) {
            OrderHead order = orderRepo.findById(Long.valueOf(id)).orElse(null);
            if (order != null && ("0".equals(order.getStatus()) || "1".equals(order.getStatus()))) {
                order.setStatus("4");
                order.setRemark(reason);
                orderRepo.save(order);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of("success", true, "canceled", count, "total", ids.size()));
    }
}
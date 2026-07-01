package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final RestTemplate rest = new RestTemplate();

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 订单统计
        try {
            var sales = rest.getForObject("http://127.0.0.1:8084/api/analytics/sales-overview", Map.class);
            data.put("sales", sales);
        } catch (Exception e) { data.put("sales", "N/A"); }

        // 营收
        try {
            var rev = rest.getForObject("http://127.0.0.1:8084/api/revenue/overview", Map.class);
            data.put("revenue", rev);
        } catch (Exception e) { data.put("revenue", "N/A"); }

        // 库存预警
        try {
            var alerts = rest.getForObject("http://127.0.0.1:8085/api/inventory/alerts", List.class);
            data.put("inventoryAlerts", alerts != null ? ((List<?>) alerts).size() : -1);
        } catch (Exception e) { data.put("inventoryAlerts", -1); }

        // 评价统计
        try {
            var reviews = rest.getForObject("http://127.0.0.1:8084/api/reviews/stats", Map.class);
            data.put("reviews", reviews);
        } catch (Exception e) { data.put("reviews", "N/A"); }

        // 支付订单数
        try {
            var payments = rest.getForObject("http://127.0.0.1:8083/api/payment/orders", List.class);
            data.put("paymentCount", payments != null ? ((List<?>) payments).size() : -1);
        } catch (Exception e) { data.put("paymentCount", -1); }

        data.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(data);
    }
}
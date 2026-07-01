package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/global-dashboard")
public class GlobalDashboardController {

    private final RestTemplate rest = new RestTemplate();

    /** 全局仪表盘: 跨6服务聚合所有核心指标 */
    @GetMapping
    public ResponseEntity<?> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        // 服务健康
        Map<String, String> health = new LinkedHashMap<>();
        int[] ports = {8080, 8081, 8083, 8084, 8085, 8090};
        String[] names = {"core", "usercenter", "payment", "order", "inventory", "gateway"};
        for (int i = 0; i < ports.length; i++) {
            try {
                var resp = rest.getForObject("http://127.0.0.1:" + ports[i] + "/actuator/health", Map.class);
                health.put(names[i], resp != null ? "UP" : "DOWN");
            } catch (Exception e) { health.put(names[i], "DOWN"); }
        }
        data.put("health", health);

        // 业务指标
        Map<String, Object> metrics = new LinkedHashMap<>();
        try { metrics.put("sales", rest.getForObject("http://127.0.0.1:8084/api/analytics/sales-overview", Map.class)); } catch (Exception e) {}
        try { metrics.put("inventoryAlerts", rest.getForObject("http://127.0.0.1:8085/api/inventory/alerts", List.class)); } catch (Exception e) {}
        try { metrics.put("paymentOrders", rest.getForObject("http://127.0.0.1:8083/api/payment/orders", List.class)); } catch (Exception e) {}
        try { metrics.put("reviews", rest.getForObject("http://127.0.0.1:8084/api/reviews/stats", Map.class)); } catch (Exception e) {}
        data.put("metrics", metrics);

        return ResponseEntity.ok(data);
    }
}
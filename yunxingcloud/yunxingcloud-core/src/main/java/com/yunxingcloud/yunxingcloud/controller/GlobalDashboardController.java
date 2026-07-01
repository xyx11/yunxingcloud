package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/global-dashboard")
public class GlobalDashboardController {

    private final RestTemplate rest;

    public GlobalDashboardController(RestTemplate rest) {
        this.rest = rest;
    }

    /** 全局仪表盘: 跨6服务聚合所有核心指标 */
    @GetMapping
    public ResponseEntity<?> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        Map<String, Object> metrics = new LinkedHashMap<>();
        try { metrics.put("sales", rest.getForObject("http://yunxingcloud-order/api/analytics/sales-overview", Map.class)); } catch (Exception e) {}
        try { metrics.put("inventoryAlerts", rest.getForObject("http://yunxingcloud-inventory/api/inventory/alerts", List.class)); } catch (Exception e) {}
        try { metrics.put("paymentOrders", rest.getForObject("http://yunxingcloud-payment/api/payment/orders", List.class)); } catch (Exception e) {}
        try { metrics.put("reviews", rest.getForObject("http://yunxingcloud-order/api/reviews/stats", Map.class)); } catch (Exception e) {}
        data.put("metrics", metrics);

        return ResponseEntity.ok(data);
    }
}
package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "管理仪表盘", description = "管理后台仪表盘")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final RestTemplate rest;

    public AdminDashboardController(RestTemplate rest) {
        this.rest = rest;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        try {
            data.put("sales", rest.getForObject("http://yunxingcloud-order/api/analytics/sales-overview", Map.class));
        } catch (Exception e) { data.put("sales", "N/A"); }

        try {
            data.put("revenue", rest.getForObject("http://yunxingcloud-order/api/revenue/overview", Map.class));
        } catch (Exception e) { data.put("revenue", "N/A"); }

        try {
            var alerts = rest.getForObject("http://yunxingcloud-inventory/api/inventory/alerts", List.class);
            data.put("inventoryAlerts", alerts != null ? alerts.size() : -1);
        } catch (Exception e) { data.put("inventoryAlerts", -1); }

        try {
            data.put("reviews", rest.getForObject("http://yunxingcloud-order/api/reviews/stats", Map.class));
        } catch (Exception e) { data.put("reviews", "N/A"); }

        try {
            var payments = rest.getForObject("http://yunxingcloud-payment/api/payment/orders", List.class);
            data.put("paymentCount", payments != null ? payments.size() : -1);
        } catch (Exception e) { data.put("paymentCount", -1); }

        data.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(data);
    }
}
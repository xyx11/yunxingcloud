package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DataScreenController {

    private final RestTemplate rest = new RestTemplate();

    /** 聚合数据大屏: 跨服务获取核心指标 */
    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        // 从 Order 服务获取销售数据
        try {
            var resp = rest.getForObject("http://localhost:8084/api/analytics/sales-overview", Map.class);
            data.put("sales", resp);
        } catch (Exception e) { data.put("sales", Map.of("error", "order service unavailable")); }

        // 从 Inventory 服务获取库存预警
        try {
            var resp = rest.getForObject("http://localhost:8085/api/inventory/alerts", List.class);
            data.put("alerts", resp != null ? resp.size() : 0);
        } catch (Exception e) { data.put("alerts", -1); }

        return ResponseEntity.ok(data);
    }
}
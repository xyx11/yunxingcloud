package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemOverviewController {

    private final RestTemplate rest = new RestTemplate();

    /** 全系统概览 (聚合6服务核心指标) */
    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        // 服务健康
        Map<String, String> services = new LinkedHashMap<>();
        int[] ports = {8080, 8081, 8083, 8084, 8085, 8090};
        String[] names = {"core", "usercenter", "payment", "order", "inventory", "gateway"};
        int up = 0;
        for (int i = 0; i < ports.length; i++) {
            try {
                var resp = rest.getForObject("http://127.0.0.1:" + ports[i] + "/actuator/health", Map.class);
                services.put(names[i], resp != null ? "UP" : "DOWN");
                up++;
            } catch (Exception e) {
                services.put(names[i], "DOWN");
            }
        }
        data.put("services", services);
        data.put("servicesUp", up);
        data.put("servicesTotal", ports.length);

        // 业务指标聚合
        Map<String, Object> business = new LinkedHashMap<>();
        try {
            var sales = rest.getForObject("http://127.0.0.1:8084/api/analytics/sales-overview", Map.class);
            business.put("sales", sales);
        } catch (Exception e) { business.put("sales", "N/A"); }
        try {
            var inventory = rest.getForObject("http://127.0.0.1:8085/api/inventory/alerts", List.class);
            business.put("alerts", inventory != null ? ((List<?>) inventory).size() : 0);
        } catch (Exception e) { business.put("alerts", -1); }
        data.put("business", business);

        // JVM 信息
        Runtime rt = Runtime.getRuntime();
        data.put("jvm", Map.of(
                "usedMemoryMB", (rt.totalMemory() - rt.freeMemory()) / 1048576,
                "maxMemoryMB", rt.maxMemory() / 1048576,
                "processors", rt.availableProcessors()
        ));

        // 版本
        data.put("version", Map.of(
                "platform", "v2.3.0",
                "java", System.getProperty("java.version"),
                "spring", "4.0.6"
        ));

        return ResponseEntity.ok(data);
    }
}
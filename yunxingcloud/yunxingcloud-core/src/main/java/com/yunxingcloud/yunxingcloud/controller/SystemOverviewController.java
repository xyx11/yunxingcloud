package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemOverviewController {

    private final RestTemplate rest;

    public SystemOverviewController(RestTemplate rest) {
        this.rest = rest;
    }

    /** 全系统概览 (聚合6服务核心指标) */
    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        // 服务健康 (跨服务 lb://)
        Map<String, String> services = new LinkedHashMap<>();
        String[][] svcList = {
            {"yunxingcloud-core", "core"},
            {"yunxingcloud-usercenter", "usercenter"},
            {"yunxingcloud-payment", "payment"},
            {"yunxingcloud-order", "order"},
            {"yunxingcloud-inventory", "inventory"},
            {"yunxingcloud-gateway", "gateway"},
        };
        int up = 0;
        for (String[] svc : svcList) {
            try {
                var resp = rest.getForObject("http://" + svc[0] + "/actuator/health", Map.class);
                services.put(svc[1], resp != null ? "UP" : "DOWN");
                up++;
            } catch (Exception e) {
                services.put(svc[1], "DOWN");
            }
        }
        data.put("services", services);
        data.put("servicesUp", up);
        data.put("servicesTotal", svcList.length);

        // 业务指标聚合
        Map<String, Object> business = new LinkedHashMap<>();
        try {
            business.put("sales", rest.getForObject("http://yunxingcloud-order/api/analytics/sales-overview", Map.class));
        } catch (Exception e) { business.put("sales", "N/A"); }
        try {
            var inventory = rest.getForObject("http://yunxingcloud-inventory/api/inventory/alerts", List.class);
            business.put("alerts", inventory != null ? inventory.size() : 0);
        } catch (Exception e) { business.put("alerts", -1); }
        data.put("business", business);

        // JVM 信息
        Runtime rt = Runtime.getRuntime();
        data.put("jvm", Map.of(
                "usedMemoryMB", (rt.totalMemory() - rt.freeMemory()) / 1048576,
                "maxMemoryMB", rt.maxMemory() / 1048576,
                "processors", rt.availableProcessors()
        ));

        return ResponseEntity.ok(data);
    }
}
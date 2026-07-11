package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "灵活查询", description = "动态SQL查询")
@RestController
@RequestMapping("/api/query")
public class FlexQueryController {

    private final RestTemplate rest;

    public FlexQueryController(RestTemplate rest) {
        this.rest = rest;
    }

    /** 灵活数据查询: 一次请求获取多个资源 */
    @PostMapping
    public ResponseEntity<?> query(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        List<String> resources = (List<String>) body.getOrDefault("resources", List.of());

        for (String resource : resources) {
            try {
                result.put(resource, fetchResource(resource, body));
            } catch (Exception e) {
                result.put(resource, Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.ok(result);
    }

    private Object fetchResource(String resource, Map<String, Object> params) {
        return switch (resource) {
            case "products" -> rest.getForObject("http://yunxingcloud-order/api/products/hot", Object.class);
            case "orders" -> rest.getForObject("http://yunxingcloud-order/api/analytics/sales-overview", Object.class);
            case "revenue" -> rest.getForObject("http://yunxingcloud-order/api/revenue/overview", Object.class);
            case "alerts" -> rest.getForObject("http://yunxingcloud-inventory/api/inventory/alerts", Object.class);
            case "reviews" -> rest.getForObject("http://yunxingcloud-order/api/reviews/stats", Object.class);
            case "campaigns" -> rest.getForObject("http://yunxingcloud-order/api/campaigns", Object.class);
            case "tickets" -> rest.getForObject("http://yunxingcloud-core/api/tickets", Object.class);
            default -> Map.of("error", "unknown resource: " + resource);
        };
    }
}
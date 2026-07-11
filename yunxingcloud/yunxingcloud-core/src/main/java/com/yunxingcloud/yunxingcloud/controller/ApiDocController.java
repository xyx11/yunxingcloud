package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "API文档", description = "API文档入口")
@RestController
@RequestMapping("/api")
public class ApiDocController {

    @GetMapping("/endpoints")
    public ResponseEntity<?> endpoints() {
        List<Map<String, Object>> apis = new ArrayList<>();

        // Core
        apis.add(ep("POST", "/api/login", "用户登录", "core"));
        apis.add(ep("POST", "/api/logout", "登出", "core"));
        apis.add(ep("POST", "/api/refresh", "刷新Token", "core"));
        apis.add(ep("GET", "/api/user", "当前用户信息", "core"));
        apis.add(ep("GET", "/api/tickets", "工单列表", "core"));
        apis.add(ep("POST", "/api/tickets", "创建工单", "core"));
        apis.add(ep("GET", "/api/system/overview", "全系统概览", "core"));
        apis.add(ep("GET", "/api/seo/meta/home", "SEO元数据", "core"));

        // Order
        apis.add(ep("GET", "/api/products", "商品列表", "order"));
        apis.add(ep("GET", "/api/products/hot", "热门商品", "order"));
        apis.add(ep("GET", "/api/cart", "购物车", "order"));
        apis.add(ep("POST", "/api/orders", "下单", "order"));
        apis.add(ep("GET", "/api/group-buy", "拼团列表", "order"));
        apis.add(ep("GET", "/api/flash-sale", "秒杀列表", "order"));
        apis.add(ep("GET", "/api/after-sale", "售后列表", "order"));
        apis.add(ep("GET", "/api/points/account", "积分账户", "order"));
        apis.add(ep("GET", "/api/recommend/hot", "热门推荐", "order"));
        apis.add(ep("GET", "/api/articles", "文章列表", "order"));
        apis.add(ep("GET", "/api/notifications", "通知列表", "order"));
        apis.add(ep("GET", "/api/campaigns", "活动列表", "order"));
        apis.add(ep("GET", "/api/social/wishlist", "心愿单", "order"));
        apis.add(ep("GET", "/api/tags", "标签列表", "order"));
        apis.add(ep("GET", "/api/member/benefits", "会员权益", "order"));
        apis.add(ep("GET", "/api/personalized/home", "个性化首页", "order"));
        apis.add(ep("GET", "/api/analytics/sales-overview", "销售概览", "order"));
        apis.add(ep("GET", "/api/reviews/stats", "评价统计", "order"));
        apis.add(ep("GET", "/api/logistics/order/{id}", "物流追踪", "order"));
        apis.add(ep("GET", "/api/compare", "商品对比", "order"));
        apis.add(ep("GET", "/api/gift-cards/{no}", "礼品卡查询", "order"));

        // Payment
        apis.add(ep("GET", "/api/payment/orders", "支付订单", "payment"));
        apis.add(ep("POST", "/api/payment/callback/{ch}", "支付回调", "payment"));

        // Inventory
        apis.add(ep("GET", "/api/inventory", "库存列表", "inventory"));
        apis.add(ep("GET", "/api/inventory/alerts", "库存预警", "inventory"));
        apis.add(ep("GET", "/api/inventory/reorder-suggestions", "补货建议", "inventory"));
        apis.add(ep("GET", "/api/warehouses", "仓库列表", "inventory"));

        return ResponseEntity.ok(Map.of("total", apis.size(), "endpoints", apis));
    }

    private Map<String, Object> ep(String method, String path, String desc, String service) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("method", method); m.put("path", path);
        m.put("description", desc); m.put("service", service);
        return m;
    }
}
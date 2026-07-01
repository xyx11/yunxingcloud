package com.yunxingcloud.order.service;

import com.yunxingcloud.order.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AnalyticsService {

    private final OrderHeadRepository orderRepo;
    private final ProductRepository productRepo;

    public AnalyticsService(OrderHeadRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    /** 销售概览 */
    public Map<String, Object> salesOverview() {
        long todayOrders = orderRepo.count(); // 简化: 总数
        long totalProducts = productRepo.count();
        return Map.of(
                "totalOrders", todayOrders,
                "totalProducts", totalProducts,
                "today", LocalDate.now().toString()
        );
    }

    /** 近7天订单趋势 (简化: 返回空数组, 需 SQL 查询) */
    public List<Map<String, Object>> orderTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            trend.add(Map.of("date", LocalDate.now().minusDays(i).toString(),
                    "count", 0, "amount", 0));
        }
        return trend;
    }

    /** 商品销量排行 */
    public List<Map<String, Object>> topProducts(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        productRepo.findAll().stream()
                .sorted((a, b) -> Integer.compare(
                                    b.getSales() != null ? b.getSales() : 0,
                                    a.getSales() != null ? a.getSales() : 0))
                .limit(limit)
                .forEach(p -> result.add(Map.of(
                        "id", p.getId(), "name", p.getName(),
                        "sales", p.getSales() != null ? p.getSales() : 0,
                        "price", p.getPrice())));
        return result;
    }
}
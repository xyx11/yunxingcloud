package com.yunxingcloud.order.service;

import com.yunxingcloud.order.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        productRepo.findByStatus("0", PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sales")))
                .forEach(p -> result.add(Map.of(
                        "id", p.getId(), "name", p.getName(),
                        "sales", p.getSales() != null ? p.getSales() : 0,
                        "price", p.getPrice())));
        return result;
    }

    /** 本周销售统计 */
    public Map<String, Object> weeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dailyStats.add(Map.of("date", weekStart.plusDays(i).toString(), "orders", 0, "revenue", 0));
        }
        return Map.of("weekStart", weekStart.toString(), "weekEnd", today.toString(), "dailyStats", dailyStats);
    }

    /** 仪表盘摘要 */
    public Map<String, Object> dashboard() {
        return Map.of(
            "totalOrders", orderRepo.count(),
            "totalProducts", productRepo.count(),
            "today", LocalDate.now().toString(),
            "topProducts", topProducts(5),
            "weekly", weeklyStats()
        );
    }
}
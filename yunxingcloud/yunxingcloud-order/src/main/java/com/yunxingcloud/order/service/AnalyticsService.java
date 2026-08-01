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
        long totalOrders = orderRepo.count();
        long totalProducts = productRepo.count();
        var recentOrders = orderRepo.findByCreatedAtAfter(
                java.time.LocalDateTime.now().minusDays(1));
        long todayOrders = recentOrders.size();
        long todayRevenue = recentOrders.stream()
                .filter(o -> !"4".equals(o.getStatus()))
                .mapToLong(o -> o.getActualAmount() != null ? o.getActualAmount() : 0).sum();
        return Map.of(
                "totalOrders", totalOrders,
                "todayOrders", todayOrders,
                "todayRevenue", todayRevenue,
                "totalProducts", totalProducts,
                "today", LocalDate.now().toString()
        );
    }

    /** 近7天订单趋势 */
    public List<Map<String, Object>> orderTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        var recentOrders = orderRepo.findByCreatedAtAfter(
                java.time.LocalDateTime.now().minusDays(7));
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            long count = recentOrders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().toString().equals(date)).count();
            long amount = recentOrders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().toString().equals(date))
                    .filter(o -> !"4".equals(o.getStatus()))
                    .mapToLong(o -> o.getActualAmount() != null ? o.getActualAmount() : 0).sum();
            trend.add(Map.of("date", date, "count", count, "amount", amount));
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
        var weekOrders = orderRepo.findByCreatedAtAfter(weekStart.atStartOfDay());
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = weekStart.plusDays(i).toString();
            long orders = weekOrders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().toString().equals(date)).count();
            long revenue = weekOrders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().toString().equals(date))
                    .filter(o -> !"4".equals(o.getStatus()))
                    .mapToLong(o -> o.getActualAmount() != null ? o.getActualAmount() : 0).sum();
            dailyStats.add(Map.of("date", date, "orders", orders, "revenue", revenue));
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
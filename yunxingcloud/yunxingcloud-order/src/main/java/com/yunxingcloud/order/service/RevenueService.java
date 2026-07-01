package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RevenueService {

    private final OrderHeadRepository orderRepo;

    public RevenueService(OrderHeadRepository orderRepo) { this.orderRepo = orderRepo; }

    /** 营收概览 */
    public Map<String, Object> overview() {
        List<OrderHead> all = orderRepo.findAll();
        long totalRevenue = all.stream().filter(o -> !"4".equals(o.getStatus()))
                .mapToLong(o -> o.getActualAmount() != null ? o.getActualAmount() : o.getTotalAmount()).sum();
        long paidRevenue = all.stream().filter(o -> "1".equals(o.getStatus()) || "2".equals(o.getStatus()) || "3".equals(o.getStatus()))
                .mapToLong(o -> o.getActualAmount() != null ? o.getActualAmount() : o.getTotalAmount()).sum();
        long totalOrders = all.size();
        long paidOrders = all.stream().filter(o -> !"0".equals(o.getStatus()) && !"4".equals(o.getStatus())).count();

        return Map.of(
                "totalRevenue", totalRevenue,
                "paidRevenue", paidRevenue,
                "totalOrders", totalOrders,
                "paidOrders", paidOrders,
                "conversionRate", totalOrders > 0 ? Math.round(100.0 * paidOrders / totalOrders) : 0
        );
    }

    /** 近30天每日营收 */
    public List<Map<String, Object>> dailyRevenue() {
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) dailyMap.put(LocalDate.now().minusDays(i).toString(), 0L);

        for (OrderHead o : orderRepo.findAll()) {
            if (o.getCreatedAt() == null) continue;
            String day = o.getCreatedAt().toLocalDate().toString();
            Long amount = o.getActualAmount() != null ? o.getActualAmount() : o.getTotalAmount();
            dailyMap.computeIfPresent(day, (k, v) -> v + amount);
        }

        return dailyMap.entrySet().stream()
                .map(e -> Map.<String, Object>of("date", e.getKey(), "revenue", e.getValue()))
                .toList();
    }
}
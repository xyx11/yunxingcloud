package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class RevenueService {

    private final OrderHeadRepository orderRepo;

    public RevenueService(OrderHeadRepository orderRepo) { this.orderRepo = orderRepo; }

    /** 营收概览 */
    @Transactional(readOnly = true)
    public Map<String, Object> overview() {
        long totalRevenue = orderRepo.totalRevenue();
        long paidRevenue = orderRepo.paidRevenue();
        long totalOrders = orderRepo.totalOrderCount();
        long paidOrders = orderRepo.paidOrderCount();

        return Map.of(
                "totalRevenue", totalRevenue,
                "paidRevenue", paidRevenue,
                "totalOrders", totalOrders,
                "paidOrders", paidOrders,
                "conversionRate", totalOrders > 0 ? Math.round(100.0 * paidOrders / totalOrders) : 0
        );
    }

    /** 近30天每日营收 */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> dailyRevenue() {
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) dailyMap.put(LocalDate.now().minusDays(i).toString(), 0L);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (OrderHead o : orderRepo.findByCreatedAtAfter(thirtyDaysAgo.atStartOfDay())) {
            String day = o.getCreatedAt().toLocalDate().toString();
            Long amount = o.getActualAmount() != null ? o.getActualAmount() : o.getTotalAmount();
            dailyMap.computeIfPresent(day, (k, v) -> v + amount);
        }

        return dailyMap.entrySet().stream()
                .map(e -> Map.<String, Object>of("date", e.getKey(), "revenue", e.getValue()))
                .toList();
    }
}
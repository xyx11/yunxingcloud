package com.yunxingcloud.inventory.service;

import com.yunxingcloud.inventory.entity.*;
import com.yunxingcloud.inventory.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryPredictionService {

    private final StockRepository stockRepo;
    private final StockLogRepository logRepo;

    public InventoryPredictionService(StockRepository stockRepo, StockLogRepository logRepo) {
        this.stockRepo = stockRepo; this.logRepo = logRepo;
    }

    /** 库存健康度报告: 预测哪些商品需要补货 */
    public List<Map<String, Object>> reorderSuggestions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        List<Stock> all = stockRepo.findAll();

        for (Stock stock : all) {
            // 计算近7天日均出库量
            List<StockLog> recentOut = logRepo.findByProductIdOrderByCreatedAtDesc(
                    stock.getProductId()).stream()
                    .filter(l -> l.getWarehouseId().equals(stock.getWarehouseId()))
                    .filter(l -> "out".equals(l.getType()) || "order_out".equals(l.getType()))
                    .toList();

            if (recentOut.isEmpty()) continue;

            int totalOut = recentOut.stream().mapToInt(StockLog::getQuantity).sum();
            double dailyAvg = totalOut / 7.0;

            // 可用库存 = 总库存 - 预占
            int available = stock.getQuantity() - stock.getReserved();

            // 可售天数
            double daysLeft = dailyAvg > 0 ? available / dailyAvg : 999;

            // 需要补货: 可售天数 < 阈值 或 库存 < 最低库存
            if (daysLeft < 7 || available <= stock.getMinQuantity()) {
                int suggestQty = (int) Math.ceil(dailyAvg * 14) - available; // 建议补到14天
                if (suggestQty > 0) {
                    suggestions.add(Map.of(
                            "productId", stock.getProductId(),
                            "productName", stock.getProductName(),
                            "warehouseId", stock.getWarehouseId(),
                            "currentStock", available,
                            "reserved", stock.getReserved(),
                            "dailyAvgOut", Math.round(dailyAvg * 10) / 10.0,
                            "daysLeft", Math.round(daysLeft * 10) / 10.0,
                            "suggestReorder", Math.max(suggestQty, stock.getMinQuantity() - available),
                            "urgency", daysLeft < 3 ? "urgent" : daysLeft < 7 ? "warning" : "info"
                    ));
                }
            }
        }
        return suggestions;
    }
}
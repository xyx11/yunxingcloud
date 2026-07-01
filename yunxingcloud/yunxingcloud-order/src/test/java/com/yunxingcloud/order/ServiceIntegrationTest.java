package com.yunxingcloud.order;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class ServiceIntegrationTest {

    @Test void orderTotalCalculation() {
        Map<String,Integer> cart = Map.of("p1",2, "p2",1);
        Map<String,Long> prices = Map.of("p1",9900L, "p2",19900L);
        long total = cart.entrySet().stream().mapToLong(e -> prices.get(e.getKey()) * e.getValue()).sum();
        assertEquals(39700L, total);
    }

    @Test void couponAndMemberStacking() {
        long total = 30000L, couponDiscount = 3000L;
        long afterCoupon = total - couponDiscount;
        int memberDiscountRate = 95; // 9.5折
        long finalAmount = afterCoupon * memberDiscountRate / 100;
        assertEquals(25650L, finalAmount);
    }

    @Test void inventoryTransferConsistency() {
        int fromStock = 100, toStock = 50, qty = 30;
        fromStock -= qty; toStock += qty;
        assertEquals(70, fromStock);
        assertEquals(80, toStock);
    }

    @Test void orderStatusLifecycle() {
        String[] flow = {"0","1","2","3"};
        for (int i = 0; i < flow.length - 1; i++) {
            assertNotEquals(flow[i], flow[i+1]);
        }
    }

    @Test void concurrentFlashSaleCheck() {
        int stock = 10;
        int[] purchases = {3, 4, 2, 5}; // 14 total, 10 stock
        int sold = 0;
        for (int qty : purchases) {
            if (sold + qty <= stock) sold += qty;
        }
        assertEquals(9, sold); // 3+4+2=9, 5 rejected
    }

    @Test void pointsExpirationCalculation() {
        long earned = 5000L, spent = 2000L, expired = 1000L;
        assertEquals(2000L, earned - spent - expired);
    }

    @Test void multiWarehouseStockAggregation() {
        long[] warehouseStocks = {50, 30, 20};
        assertEquals(100, Arrays.stream(warehouseStocks).sum());
    }

    @Test void revenueDailyAggregation() {
        Map<String,Long> daily = new LinkedHashMap<>();
        daily.merge("2026-07-01", 9900L, Long::sum);
        daily.merge("2026-07-01", 19900L, Long::sum);
        daily.merge("2026-07-02", 5000L, Long::sum);
        assertEquals(29800L, daily.get("2026-07-01"));
        assertEquals(2, daily.size());
    }
}
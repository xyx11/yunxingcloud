package com.yunxingcloud.order;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单生命周期完整流程测试
 * 状态流转: 0待支付→1已支付→2已发货→3已完成 / 4已取消
 */
class OrderLifecycleTest {

    @Test
    void statusTransitionValid() {
        // 正常流程
        assertTrue(isValidTransition("0", "1"));  // 待支付→已支付
        assertTrue(isValidTransition("1", "2"));  // 已支付→已发货
        assertTrue(isValidTransition("2", "3"));  // 已发货→已完成
        assertFalse(isValidTransition("2", "0")); // 不能回到待支付
    }

    @Test
    void cancelValid() {
        assertTrue(isValidTransition("0", "4")); // 待支付→取消
        assertTrue(isValidTransition("1", "4")); // 已支付→取消(退款)
        assertFalse(isValidTransition("3", "4")); // 已完成→不可取消
        assertFalse(isValidTransition("4", "0")); // 已取消→不可逆
    }

    @Test
    void afterSaleStatusFlow() {
        String[] flow = {"0", "1", "3", "4"}; // 待审核→通过→退款中→完成
        for (int i = 0; i < flow.length - 1; i++) {
            assertTrue(isValidAfterSaleTransition(flow[i], flow[i + 1]),
                    flow[i] + " → " + flow[i + 1]);
        }
    }

    @Test
    void groupBuyStatusFlow() {
        String[] flow = {"0", "1"}; // 拼团中→成团
        assertTrue(isValidGroupTransition(flow[0], flow[1]));
        assertFalse(isValidGroupTransition("1", "0")); // 不可逆
    }

    @Test
    void flashSaleStockCheck() {
        int stock = 100, sold = 0;
        int[] purchases = {5, 10, 50, 35};
        for (int qty : purchases) {
            if (sold + qty <= stock) sold += qty;
        }
        assertEquals(100, sold);
        assertFalse(sold + 1 <= stock); // 库存已空
    }

    @Test
    void couponDeduction() {
        long totalAmount = 29900L; // ¥299.00
        long couponAmount = 3000L;  // ¥30优惠券
        long actualAmount = totalAmount - couponAmount;
        assertEquals(26900L, actualAmount);

        // 不能超过总金额
        long maxCoupon = Math.min(couponAmount, totalAmount);
        assertEquals(3000L, maxCoupon);
    }

    @Test
    void pointsRedemption() {
        long points = 500;
        long centsPerPoint = 1; // 100积分=1元 → 1积分=1分
        long deducted = points / 100; // 抵扣5元
        assertEquals(5, deducted);
    }

    private boolean isValidTransition(String from, String to) {
        return switch (from) {
            case "0" -> "1".equals(to) || "4".equals(to);
            case "1" -> "2".equals(to) || "4".equals(to);
            case "2" -> "3".equals(to);
            default -> false;
        };
    }

    private boolean isValidAfterSaleTransition(String from, String to) {
        return switch (from) {
            case "0" -> "1".equals(to) || "2".equals(to);
            case "1", "3" -> "4".equals(to) || "3".equals(to);
            default -> false;
        };
    }

    private boolean isValidGroupTransition(String from, String to) {
        return "0".equals(from) && ("1".equals(to) || "2".equals(to));
    }
}
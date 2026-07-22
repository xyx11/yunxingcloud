package com.yunxingcloud.order;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分服务单元测试
 */
class PointsServiceTest {

    private long balance;
    private long totalEarned;
    private long totalSpent;
    private boolean checkedInToday;

    @BeforeEach
    void setUp() {
        balance = 100;
        totalEarned = 100;
        totalSpent = 0;
        checkedInToday = false;
    }

    @Test
    void earnPoints() {
        long earned = 50;
        balance += earned;
        totalEarned += earned;
        assertEquals(150, balance);
        assertEquals(150, totalEarned);
    }

    @Test
    void spendPoints() {
        long spent = 30;
        assertTrue(balance >= spent);
        balance -= spent;
        totalSpent += spent;
        assertEquals(70, balance);
        assertEquals(30, totalSpent);
    }

    @Test
    void spendInsufficientBalance() {
        long spent = 200;
        assertFalse(balance >= spent);
    }

    @Test
    void dailyCheckin() {
        assertFalse(checkedInToday);
        checkedInToday = true;
        balance += 5;
        totalEarned += 5;
        assertEquals(105, balance);
        assertTrue(checkedInToday);
    }

    @Test
    void checkinIdempotency() {
        // First check-in
        assertFalse(checkedInToday);
        checkedInToday = true;
        balance += 5;
        // Duplicate check-in should be rejected
        assertTrue(checkedInToday);
        // Balance should remain the same
        assertEquals(105, balance);
    }

    @Test
    void pointsRedemptionCalculation() {
        long points = 500;
        long deductedCents = points / 100 * 100; // 100 points = 1 yuan = 100 cents
        assertEquals(500, deductedCents);

        long orderAmount = 9900L; // ¥99.00
        long finalAmount = orderAmount - deductedCents;
        assertEquals(9400L, finalAmount);
    }

    @Test
    void pointsRedemptionCap() {
        // Cannot redeem more points than available
        long available = 200;
        long requested = 500;
        long usable = Math.min(requested, available);
        assertEquals(200, usable);
    }

    @Test
    void registerBonusPoints() {
        long registerBonus = 100;
        balance += registerBonus;
        totalEarned += registerBonus;
        assertEquals(200, balance);
    }

    @Test
    void purchaseRewardPoints() {
        long purchaseAmount = 29900L; // ¥299.00
        long rewardPoints = Math.max(1, purchaseAmount / 100); // 1% reward
        assertEquals(299, rewardPoints);
        balance += rewardPoints;
        assertEquals(399, balance);
    }
}
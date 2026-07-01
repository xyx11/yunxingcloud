package com.yunxingcloud.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentClientFallbackTest {

    private PaymentClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new PaymentClientFallback();
    }

    @Test
    void createOrderShouldReturnNull() {
        assertNull(fallback.createOrder(Map.of("amount", 100)));
    }

    @Test
    void payShouldReturnFailWithFallbackFlag() {
        Map<String, Object> result = fallback.pay(1L, Map.of("method", "wechat"));
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals(true, result.get("fallback"));
    }

    @Test
    void getOrderShouldReturnFallbackData() {
        Map<String, Object> result = fallback.getOrder(100L);
        assertNotNull(result);
        assertEquals(100L, result.get("id"));
        assertEquals("0", result.get("status"));
        assertEquals(true, result.get("fallback"));
    }
}
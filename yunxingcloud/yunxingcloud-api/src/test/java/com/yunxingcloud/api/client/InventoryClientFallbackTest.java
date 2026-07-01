package com.yunxingcloud.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryClientFallbackTest {

    private InventoryClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new InventoryClientFallback();
    }

    @Test
    void orderOutShouldReturnFailWithFallbackFlag() {
        Map<String, Object> result = fallback.orderOut(Map.of("productId", 1, "quantity", 5));
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals(true, result.get("fallback"));
        assertNotNull(result.get("message"));
    }

    @Test
    void orderOutWithoutProductIdShouldNotThrow() {
        Map<String, Object> result = fallback.orderOut(Map.of());
        assertNotNull(result);
        assertEquals(false, result.get("success"));
    }

    @Test
    void orderBackShouldReturnFailWithFallbackFlag() {
        Map<String, Object> result = fallback.orderBack(Map.of("productId", 2, "quantity", 3));
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals(true, result.get("fallback"));
        assertEquals("Inventory service unavailable", result.get("message"));
    }
}
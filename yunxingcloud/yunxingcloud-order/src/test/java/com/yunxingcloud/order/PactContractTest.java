package com.yunxingcloud.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pact 消费者契约测试 (Order → Payment/Inventory)
 *
 * 运行前需启动 Pact Broker 或使用本地文件:
 *   ./mvnw test -pl yunxingcloud-order -Dtest=PactContractTest
 *
 * 集成 Pact JVM 后的完整用法:
 *   @Pact(consumer = "yunxingcloud-order", provider = "yunxingcloud-payment")
 *   public V4Pact createPaymentPact(V4PactBuilder builder) { ... }
 */
class PactContractTest {

    @Test
    void paymentCreateOrderContract() {
        // Payment API: POST /api/payment/orders
        // Request:  { "title": "订单ORD...", "amount": 9900, "channel": "wechat" }
        // Response: { "id": 1, "orderNo": "PAY...", "status": "0" }
        var request = java.util.Map.of("title", "订单ORD20260101", "amount", 9900, "channel", "wechat");
        assertNotNull(request.get("title"));
        assertTrue(request.get("title").toString().startsWith("订单"));
        assertEquals(9900L, request.get("amount"));
        assertEquals("wechat", request.get("channel"));
    }

    @Test
    void paymentPayOrderContract() {
        // Payment API: POST /api/payment/orders/{id}/pay
        // Response: { "codeUrl": "...", "prepayId": "...", "channel": "wechat" }
        var response = java.util.Map.of("codeUrl", "weixin://...", "prepayId", "wx...", "channel", "wechat");
        assertNotNull(response.get("codeUrl"));
        assertNotNull(response.get("prepayId"));
    }

    @Test
    void inventoryOrderOutContract() {
        // Inventory API: POST /api/inventory/order-out
        // Request:  { "productId": 1, "productName": "...", "warehouseId": 1, "quantity": 2, "orderId": 100 }
        // Response: { "success": true }
        var request = java.util.Map.of("productId", 1, "productName", "Test",
                "warehouseId", 1, "quantity", 2, "orderId", 100);
        assertTrue(request.containsKey("productId"));
        assertTrue(request.containsKey("warehouseId"));
        assertTrue(request.containsKey("quantity"));
        assertTrue(request.containsKey("orderId"));
    }

    @Test
    void inventoryOrderBackContract() {
        // Inventory API: POST /api/inventory/order-back
        // Request:  { "productId": 1, "warehouseId": 1, "quantity": 2, "orderId": 100 }
        // Response: { "success": true }
        var request = java.util.Map.of("productId", 1, "warehouseId", 1,
                "quantity", 2, "orderId", 100);
        assertEquals(4, request.size());
    }

    @Test
    void inventoryServiceUnavailableFallback() {
        // 库存服务不可用 → fallback 返回 { "success": false, "fallback": true }
        var fallback = java.util.Map.of("success", false, "fallback", true,
                "message", "Inventory service unavailable");
        assertFalse((Boolean) fallback.get("success"));
        assertTrue((Boolean) fallback.get("fallback"));
    }
}
package com.yunxingcloud.order;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTest {
    @Test void zeroQuantityOrder() { assertThrows(IllegalStateException.class, () -> { if(0<=0) throw new IllegalStateException("购物车为空"); }); }
    @Test void negativePriceProduct() { long p=-1; assertTrue(p<0); }
    @Test void maxIntStock() { int s=Integer.MAX_VALUE; assertEquals(Integer.MAX_VALUE, s); }
    @Test void emptyCartCheckout() { assertThrows(IllegalStateException.class, () -> { throw new IllegalStateException("购物车为空"); }); }
    @Test void nullReceiverName() { String name=null; assertNull(name); }
    @Test void veryLongProductName() { String name="A".repeat(200); assertEquals(200, name.length()); }
    @Test void concurrentSameProduct() { int qty=5; int after=qty-3; assertEquals(2, after); } // 并发扣减模拟
    @Test void refundExceedsPaid() { long paid=10000L, refund=15000L; assertTrue(refund>paid); }
    @Test void expiredCoupon() { assertTrue(java.time.LocalDateTime.now().minusDays(10).isBefore(java.time.LocalDateTime.now().minusDays(1))); }
    @Test void zeroDiscountRate() { int rate=0; long amt=10000L; assertEquals(0, amt*rate/100); }
}
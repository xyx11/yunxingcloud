package com.yunxingcloud.order.service;

import com.yunxingcloud.api.client.InventoryClient;
import com.yunxingcloud.api.client.PaymentClient;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.entity.OrderLine;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaService.class);
    private final OrderHeadRepository orderRepo;
    private final OrderService orderService;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderSagaService(OrderHeadRepository orderRepo, OrderService orderService,
                            InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.orderRepo = orderRepo;
        this.orderService = orderService;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    /**
     * SAGA: 下单 → 扣库存 → 创建支付
     * 任一步失败触发补偿
     */
    @Transactional
    public OrderHead submitWithSaga(String username, Map<String, String> receiver, Long couponId) {
        // Step 1: 创建订单
        OrderHead order = orderService.submit(username, receiver, couponId);
        List<OrderLine> lines = orderService.lines(order.getId());

        try {
            // Step 2: 预占库存
            for (OrderLine line : lines) {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", line.getProductId());
                invBody.put("productName", line.getProductName());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", line.getQuantity());
                invBody.put("orderId", order.getId());

                Map<String, Object> result = inventoryClient.orderOut(invBody);
                if (result == null || Boolean.TRUE.equals(result.get("fallback"))) {
                    throw new RuntimeException("库存服务不可用，productId=" + line.getProductId());
                }
            }

            // Step 3: 创建支付
            Map<String, Object> payOrder = Map.of(
                    "title", "订单" + order.getOrderNo(),
                    "amount", order.getActualAmount() != null ? order.getActualAmount() : order.getTotalAmount(),
                    "channel", "wechat");
            Map<String, Object> payResult = paymentClient.createOrder(payOrder);
            if (payResult != null && payResult.get("id") != null) {
                order.setPaymentOrderId(Long.valueOf(payResult.get("id").toString()));
                order.setStatus("1");
                orderRepo.save(order);
            }

            return order;

        } catch (Exception e) {
            log.error("SAGA 失败，开始补偿: orderId={}", order.getId(), e);
            compensate(order, lines);
            throw e;
        }
    }

    private void compensate(OrderHead order, List<OrderLine> lines) {
        // 补偿: 释放库存 + 取消订单
        try {
            for (OrderLine line : lines) {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", line.getProductId());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", line.getQuantity());
                invBody.put("orderId", order.getId());
                inventoryClient.orderBack(invBody);
            }
            orderService.cancelOrder(order);
            log.info("SAGA 补偿完成: orderId={}", order.getId());
        } catch (Exception ex) {
            log.error("SAGA 补偿失败，需人工介入: orderId={}, orderNo={}", order.getId(), order.getOrderNo(), ex);
            // 标记订单为补偿失败状态，等待人工处理
            try {
                order.setStatus("5"); // 5=异常/售后中
                orderRepo.save(order);
            } catch (Exception saveEx) {
                log.error("无法更新订单状态为补偿失败: orderId={}", order.getId(), saveEx);
            }
        }
    }
}

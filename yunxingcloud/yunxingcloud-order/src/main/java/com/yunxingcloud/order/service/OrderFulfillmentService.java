package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderFulfillmentService {

    private final OrderHeadRepository orderRepo;
    private final OrderShipmentRepository shipmentRepo;

    public OrderFulfillmentService(OrderHeadRepository orderRepo, OrderShipmentRepository shipmentRepo) {
        this.orderRepo = orderRepo; this.shipmentRepo = shipmentRepo;
    }

    /** 发货: 创建物流单 + 更新订单状态 */
    @Transactional
    public OrderShipment ship(Long orderId, String carrier, String trackingNo) {
        OrderHead order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!"1".equals(order.getStatus())) throw new IllegalStateException("仅已支付订单可发货");
        order.setStatus("2");
        orderRepo.save(order);

        OrderShipment shipment = new OrderShipment();
        shipment.setOrderId(orderId);
        shipment.setCarrier(carrier);
        shipment.setTrackingNo(trackingNo);
        shipment.setStatus("shipped");
        shipment.setCreatedAt(LocalDateTime.now());
        return shipmentRepo.save(shipment);
    }

    /** 确认收货 */
    @Transactional
    public void confirmReceive(Long orderId) {
        OrderHead order = orderRepo.findById(orderId).orElseThrow();
        if (!"2".equals(order.getStatus())) throw new IllegalStateException("仅已发货订单可确认收货");
        order.setStatus("3");
        orderRepo.save(order);
    }
}
package com.yunxingcloud.order.event;

import com.yunxingcloud.order.service.OrderMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderMailService mailService;

    public OrderEventListener(OrderMailService mailService) {
        this.mailService = mailService;
    }

    private String userEmail(com.yunxingcloud.order.entity.OrderHead order) {
        // TODO: lookup user email from usercenter service
        return order.getUsername(); // fallback to username
    }

    @Async
    @EventListener
    public void onOrderCreated(OrderEvent.Created event) {
        log.info("事件: 订单创建 orderNo={}", event.getOrder().getOrderNo());
        mailService.sendOrderPlaced(event.getOrder(), userEmail(event.getOrder()));
    }

    @Async
    @EventListener
    public void onOrderPaid(OrderEvent.Paid event) {
        log.info("事件: 订单支付 orderNo={}", event.getOrder().getOrderNo());
        mailService.sendPaymentSuccess(event.getOrder(), userEmail(event.getOrder()));
    }

    @Async
    @EventListener
    public void onOrderShipped(OrderEvent.Shipped event) {
        log.info("事件: 订单发货 orderNo={}", event.getOrder().getOrderNo());
        mailService.sendShipped(event.getOrder(), userEmail(event.getOrder()), event.getCarrier(), event.getTrackingNo());
    }

    @Async
    @EventListener
    public void onOrderCanceled(OrderEvent.Canceled event) {
        log.info("事件: 订单取消 orderNo={} reason={}", event.getOrder().getOrderNo(), event.getReason());
        mailService.sendOrderCanceled(event.getOrder(), userEmail(event.getOrder()));
    }
}
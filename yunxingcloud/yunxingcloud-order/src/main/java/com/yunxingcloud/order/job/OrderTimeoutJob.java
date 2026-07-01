package com.yunxingcloud.order.job;

import com.yunxingcloud.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OrderTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutJob.class);
    private final OrderService orderService;

    public OrderTimeoutJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void cancelTimeoutOrders() {
        try {
            orderService.cancelTimeoutOrders();
        } catch (Exception e) {
            log.error("超时订单取消任务异常: {}", e.getMessage());
        }
    }
}
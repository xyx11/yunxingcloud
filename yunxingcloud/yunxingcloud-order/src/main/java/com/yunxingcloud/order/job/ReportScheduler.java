package com.yunxingcloud.order.job;

import com.yunxingcloud.order.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);
    private final OrderHeadRepository orderRepo;
    private final ProductRepository productRepo;

    public ReportScheduler(OrderHeadRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo; this.productRepo = productRepo;
    }

    /** 每日凌晨 2:00 生成运营日报 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyReport() {
        long totalOrders = orderRepo.count();
        long totalProducts = productRepo.count();

        log.info("══════ 运营日报 ══════");
        log.info("  总订单: {}", totalOrders);
        log.info("  总商品: {}", totalProducts);
        log.info("  生成时间: {}", java.time.LocalDateTime.now());
        log.info("══════════════════════");
    }
}
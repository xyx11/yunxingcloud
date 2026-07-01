package com.yunxingcloud.order.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchEmailJob {

    private static final Logger log = LoggerFactory.getLogger(BatchEmailJob.class);

    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyPromotion() {
        log.info("[定时任务] 每日9:00 促销邮件推送");
        // 对接邮件服务批量发送
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void abandonedCartReminder() {
        log.info("[定时任务] 每日10:00 购物车放弃提醒");
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void flashSalePreview() {
        log.info("[定时任务] 每日18:00 明日秒杀预告");
    }
}
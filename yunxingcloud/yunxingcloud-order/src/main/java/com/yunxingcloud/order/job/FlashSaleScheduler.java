package com.yunxingcloud.order.job;

import com.yunxingcloud.order.entity.FlashSale;
import com.yunxingcloud.order.repository.FlashSaleRepository;
import com.yunxingcloud.order.service.FlashSaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FlashSaleScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlashSaleScheduler.class);
    private final FlashSaleRepository flashRepo;
    private final FlashSaleService flashService;

    public FlashSaleScheduler(FlashSaleRepository flashRepo, FlashSaleService flashService) {
        this.flashRepo = flashRepo;
        this.flashService = flashService;
    }

    /** 每 10s 检查: 秒杀开始 → 预热库存, 秒杀结束 → 标记结束 */
    @Scheduled(fixedDelay = 10000)
    public void checkStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 自动开始
        List<FlashSale> starting = flashRepo.findByStatusAndStartTimeBefore("0", now);
        for (FlashSale fs : starting) {
            fs.setStatus("1");
            flashRepo.save(fs);
            flashService.preheat(fs.getId());
            log.info("秒杀开始: id={} product={}", fs.getId(), fs.getProductId());
        }

        // 自动结束
        List<FlashSale> ending = flashRepo.findByStatusAndStartTimeBefore("1", now);
        for (FlashSale fs : ending) {
            if (fs.getEndTime().isBefore(now)) {
                fs.setStatus("2");
                flashRepo.save(fs);
                log.info("秒杀结束: id={} sold={}", fs.getId(), fs.getSold());
            }
        }
    }
}
package com.yunxingcloud.order.config;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final ProductTagRepository tagRepo;
    private final CampaignRepository campaignRepo;
    private final GroupBuyRepository groupBuyRepo;

    public DemoDataInitializer(ProductTagRepository tagRepo, CampaignRepository campaignRepo,
                                GroupBuyRepository groupBuyRepo) {
        this.tagRepo = tagRepo; this.campaignRepo = campaignRepo; this.groupBuyRepo = groupBuyRepo;
    }

    @Override
    public void run(String... args) {
        if (tagRepo.count() > 0) return; // 已有数据, 跳过

        log.info("初始化 Demo 数据...");

        // 标签
        ProductTag hot = createTag("热卖", "#e4393c", 1);
        ProductTag newTag = createTag("新品", "#ff9800", 2);
        ProductTag free = createTag("包邮", "#4caf50", 3);
        createTag("限时", "#2196f3", 4);
        createTag("特惠", "#9c27b0", 5);

        // 营销活动
        Campaign c1 = new Campaign();
        c1.setName("618满200减30"); c1.setType("full_reduction");
        c1.setThreshold(20000L); c1.setDiscount(3000L);
        c1.setStartTime(LocalDateTime.now().minusDays(1));
        c1.setEndTime(LocalDateTime.now().plusDays(7));
        c1.setStatus("1"); c1.setLimitPerUser(3);
        campaignRepo.save(c1);

        Campaign c2 = new Campaign();
        c2.setName("新人9折"); c2.setType("discount");
        c2.setDiscount(10L); c2.setMaxDiscount(5000L);
        c2.setStartTime(LocalDateTime.now().minusDays(30));
        c2.setEndTime(LocalDateTime.now().plusDays(30));
        c2.setStatus("1"); c2.setLimitPerUser(1);
        campaignRepo.save(c2);

        log.info("Demo 数据初始化完成: {} 标签, {} 活动", tagRepo.count(), campaignRepo.count());
    }

    private ProductTag createTag(String name, String color, int sort) {
        ProductTag tag = new ProductTag();
        tag.setName(name); tag.setColor(color); tag.setSortOrder(sort);
        return tagRepo.save(tag);
    }
}
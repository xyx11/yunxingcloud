package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.PriceAlert;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.PriceAlertRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PriceAlertService {

    private final PriceAlertRepository alertRepo;
    private final ProductRepository productRepo;

    public PriceAlertService(PriceAlertRepository alertRepo, ProductRepository productRepo) {
        this.alertRepo = alertRepo; this.productRepo = productRepo;
    }

    @Transactional
    public PriceAlert create(String username, Long productId, Long targetPrice) {
        if (alertRepo.existsByUsernameAndProductId(username, productId))
            throw new IllegalStateException("已设置提醒");
        PriceAlert a = new PriceAlert();
        a.setUsername(username); a.setProductId(productId); a.setTargetPrice(targetPrice);
        return alertRepo.save(a);
    }

    public List<Map<String, Object>> checkAndNotify(String username) {
        List<PriceAlert> alerts = alertRepo.findByUsernameAndNotifiedFalse(username);
        List<Map<String, Object>> triggered = new ArrayList<>();
        for (PriceAlert a : alerts) {
            Product p = productRepo.findById(a.getProductId()).orElse(null);
            if (p != null && p.getPrice() <= a.getTargetPrice()) {
                triggered.add(Map.of(
                        "productId", p.getId(), "productName", p.getName(),
                        "currentPrice", p.getPrice(), "targetPrice", a.getTargetPrice(),
                        "saved", a.getTargetPrice() - p.getPrice()));
                a.setNotified(true);
                alertRepo.save(a);
            }
        }
        return triggered;
    }
}
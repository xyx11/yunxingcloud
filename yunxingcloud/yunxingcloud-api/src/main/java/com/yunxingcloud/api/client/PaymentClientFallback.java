package com.yunxingcloud.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentClientFallback implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallback.class);

    @Override
    public Map<String, Object> createOrder(Map<String, Object> body) {
        log.warn("Payment service unavailable, createOrder fallback");
        return null;
    }

    @Override
    public Map<String, Object> pay(Long id, Map<String, String> body) {
        log.warn("Payment service unavailable, pay fallback for order {}", id);
        return Map.of("success", false, "fallback", true);
    }

    @Override
    public Map<String, Object> getOrder(Long id) {
        log.warn("Payment service unavailable, getOrder fallback for {}", id);
        return Map.of("id", id, "status", "0", "fallback", true);
    }
}
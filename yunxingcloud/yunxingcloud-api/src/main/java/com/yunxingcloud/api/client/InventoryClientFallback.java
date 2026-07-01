package com.yunxingcloud.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryClientFallback implements InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClientFallback.class);

    @Override
    public Map<String, Object> orderOut(Map<String, Object> body) {
        log.warn("Inventory service unavailable, orderOut fallback for product {}",
                body.getOrDefault("productId", "?"));
        return Map.of("success", false, "fallback", true, "message", "Inventory service unavailable");
    }

    @Override
    public Map<String, Object> orderBack(Map<String, Object> body) {
        log.warn("Inventory service unavailable, orderBack fallback for product {}",
                body.getOrDefault("productId", "?"));
        return Map.of("success", false, "fallback", true, "message", "Inventory service unavailable");
    }
}
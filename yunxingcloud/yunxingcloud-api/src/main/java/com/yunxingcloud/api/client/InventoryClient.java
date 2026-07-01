package com.yunxingcloud.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "yunxingcloud-inventory", contextId = "inventoryClient", path = "/api/inventory",
             fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @PostMapping("/order-out")
    Map<String, Object> orderOut(@RequestBody Map<String, Object> body);

    @PostMapping("/order-back")
    Map<String, Object> orderBack(@RequestBody Map<String, Object> body);
}

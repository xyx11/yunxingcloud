package com.yunxingcloud.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "yunxingcloud-payment", contextId = "paymentClient", path = "/api/payment",
             fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/orders")
    Map<String, Object> createOrder(@RequestBody Map<String, Object> body);

    @PostMapping("/orders/{id}/pay")
    Map<String, Object> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body);

    @GetMapping("/orders/{id}")
    Map<String, Object> getOrder(@PathVariable Long id);
}

package com.yunxingcloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("/api/aggregate")
public class AggregationController {

    private static final Logger log = LoggerFactory.getLogger(AggregationController.class);
    private final WebClient.Builder webClientBuilder;

    public AggregationController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 订单详情聚合：订单 + 支付 + 物流 一次返回
     */
    @GetMapping("/order/{id}")
    public Mono<ResponseEntity<?>> orderDetail(@PathVariable Long id,
                                                @RequestHeader("Authorization") String auth) {
        WebClient client = webClientBuilder.build();

        Mono<Map> orderMono = client.get()
                .uri("lb://yunxingcloud-order/api/orders/" + id)
                .header("Authorization", auth)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())));

        Mono<Map> paymentMono = client.get()
                .uri("lb://yunxingcloud-payment/api/payment/orders?orderNo=" + id)
                .header("Authorization", auth)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.just(Collections.emptyMap()));

        Mono<Map> shipmentMono = client.get()
                .uri("lb://yunxingcloud-order/api/shipments?orderId=" + id)
                .header("Authorization", auth)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.just(Collections.emptyMap()));

        return Mono.zip(orderMono, paymentMono, shipmentMono)
                .map(tuple -> {
                    Map<String, Object> aggregated = new LinkedHashMap<>();
                    aggregated.put("order", tuple.getT1());
                    aggregated.put("payment", tuple.getT2());
                    aggregated.put("shipment", tuple.getT3());
                    return ResponseEntity.ok(aggregated);
                });
    }

    /**
     * 首页聚合：热门商品 + 新品 + 分类 + 横幅 一次返回
     */
    @GetMapping("/home")
    public Mono<ResponseEntity<?>> homeData() {
        WebClient client = webClientBuilder.build();

        Mono<Object> hot = client.get().uri("lb://yunxingcloud-order/api/products/hot")
                .retrieve().bodyToMono(Object.class).onErrorResume(e -> Mono.just(List.of()));
        Mono<Object> news = client.get().uri("lb://yunxingcloud-order/api/products/new")
                .retrieve().bodyToMono(Object.class).onErrorResume(e -> Mono.just(List.of()));
        Mono<Object> categories = client.get().uri("lb://yunxingcloud-order/api/categories")
                .retrieve().bodyToMono(Object.class).onErrorResume(e -> Mono.just(List.of()));
        Mono<Object> banners = client.get().uri("lb://yunxingcloud-order/api/banners")
                .retrieve().bodyToMono(Object.class).onErrorResume(e -> Mono.just(List.of()));

        return Mono.zip(hot, news, categories, banners)
                .map(tuple -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("hotProducts", tuple.getT1());
                    data.put("newProducts", tuple.getT2());
                    data.put("categories", tuple.getT3());
                    data.put("banners", tuple.getT4());
                    return ResponseEntity.ok(data);
                });
    }
}

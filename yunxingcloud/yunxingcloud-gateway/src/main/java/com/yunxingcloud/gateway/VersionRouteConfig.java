package com.yunxingcloud.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VersionRouteConfig {

    @Bean
    public RouteLocator versionRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // v1 → Core 服务 (默认)
                .route("v1-api", r -> r
                        .path("/api/v1/**")
                        .filters(f -> f.stripPrefix(1)  // /api/v1/xxx → /api/xxx
                                .addResponseHeader("X-API-Version", "v1"))
                        .uri("lb://yunxingcloud-core"))

                // v2 → 订单服务新版
                .route("v2-orders", r -> r
                        .path("/api/v2/orders/**")
                        .filters(f -> f.setPath("/api/orders/${remaining}")
                                .addResponseHeader("X-API-Version", "v2"))
                        .uri("lb://yunxingcloud-order"))

                // v2 → 支付服务新版
                .route("v2-payment", r -> r
                        .path("/api/v2/payment/**")
                        .filters(f -> f.setPath("/api/payment/${remaining}")
                                .addResponseHeader("X-API-Version", "v2"))
                        .uri("lb://yunxingcloud-payment"))

                .build();
    }
}
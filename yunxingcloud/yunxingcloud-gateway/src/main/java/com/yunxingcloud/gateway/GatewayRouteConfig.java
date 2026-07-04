package com.yunxingcloud.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Value("${nacos.enabled:false}")
    private boolean nacosEnabled;

    private String uri(String service, int port) {
        if (nacosEnabled) {
            return "lb://" + service;
        }
        return "http://127.0.0.1:" + port;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth -> core
                .route("core-auth", r -> r
                        .path("/api/login", "/api/refresh", "/api/logout", "/api/csrf", "/api/captcha", "/api/publicKey")
                        .uri(uri("yunxingcloud-core", 8080)))
                // Register -> usercenter
                .route("usercenter-register", r -> r
                        .path("/api/register")
                        .uri(uri("yunxingcloud-usercenter", 8081)))
                // OAuth2 -> usercenter
                .route("usercenter-oauth2", r -> r
                        .path("/oauth2/**", "/login/**")
                        .uri(uri("yunxingcloud-usercenter", 8081)))
                // OIDC -> usercenter
                .route("usercenter-wellknown", r -> r
                        .path("/.well-known/**")
                        .uri(uri("yunxingcloud-usercenter", 8081)))
                // User social accounts -> usercenter
                .route("usercenter-social", r -> r
                        .path("/api/user/social/**")
                        .uri(uri("yunxingcloud-usercenter", 8081)))
                // Order -> order service
                .route("order-api", r -> r
                        .path("/api/products/**", "/api/cart/**", "/api/orders/**",
                              "/api/categories/**", "/api/brands/**", "/api/coupons/**",
                              "/api/addresses/**", "/api/shipments/**", "/api/skus/**",
                              "/api/banners/**", "/api/favorites/**",
                              "/api/group-buy/**", "/api/flash-sale/**",
                              "/api/after-sale/**", "/api/invoices/**",
                              "/api/points/**", "/api/recommend/**",
                              "/api/articles/**", "/api/notifications/**",
                              "/api/analytics/**",
                              "/api/logistics/**", "/api/compare/**",
                              "/api/gift-cards/**",
                              "/api/campaigns/**", "/api/social/**",
                              "/api/personalized/**", "/api/reviews/**",
                              "/api/tags/**", "/api/member/**",
                              "/api/feedback/**", "/api/price-alert/**",
                              "/api/bundles/**", "/api/suppliers/**",
                              "/api/batch/**", "/api/chat/**",
                              "/api/revenue/**")
                        .uri(uri("yunxingcloud-order", 8084)))
                // Export + GlobalDashboard -> core
                .route("core-export", r -> r
                        .path("/api/export/**", "/api/global-dashboard/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                // System overview -> core
                .route("core-system", r -> r
                        .path("/api/system/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                // Data dashboard -> core
                .route("core-dashboard", r -> r
                        .path("/api/dashboard/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                // Inventory -> inventory service
                .route("inventory-api", r -> r
                        .path("/api/warehouses/**", "/api/inventory/**")
                        .uri(uri("yunxingcloud-inventory", 8085)))
                // Payment -> payment service
                .route("payment-api", r -> r
                        .path("/api/payment/**")
                        .uri(uri("yunxingcloud-payment", 8083)))
                // Password -> core
                .route("core-password", r -> r
                        .path("/api/password/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                // Business API -> core
                .route("core-api", r -> r
                        .path("/api/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                // SPA -> core
                .route("core-spa", r -> r
                        .path("/**")
                        .uri(uri("yunxingcloud-core", 8080)))
                .build();
    }
}
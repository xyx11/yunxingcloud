package com.yunxingcloud.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private static final String CORE = "lb://yunxingcloud-core";
    private static final String UC = "lb://yunxingcloud-usercenter";
    private static final String PAY = "lb://yunxingcloud-payment";
    private static final String ORD = "lb://yunxingcloud-order";
    private static final String INV = "lb://yunxingcloud-inventory";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth -> core
                .route("core-auth", r -> r
                        .path("/api/login", "/api/refresh", "/api/logout", "/api/csrf", "/api/captcha", "/api/publicKey")
                        .uri(CORE))
                // Register -> usercenter
                .route("usercenter-register", r -> r
                        .path("/api/register")
                        .uri(UC))
                // OAuth2 -> usercenter
                .route("usercenter-oauth2", r -> r
                        .path("/oauth2/**", "/login/**")
                        .uri(UC))
                // OIDC -> usercenter
                .route("usercenter-wellknown", r -> r
                        .path("/.well-known/**")
                        .uri(UC))
                // User social accounts -> usercenter
                .route("usercenter-social", r -> r
                        .path("/api/user/social/**")
                        .uri(UC))
                // Order -> order service
                .route("order-api", r -> r
                        .path("/api/products/**", "/api/cart/**", "/api/orders/**",
                              "/api/categories/**", "/api/brands/**", "/api/coupons/**",
                              "/api/addresses/**", "/api/shipments/**", "/api/skus/**",
                              "/api/banners/**", "/api/favorites/**",
                              // v2.2+
                              "/api/group-buy/**", "/api/flash-sale/**",
                              "/api/after-sale/**", "/api/invoices/**",
                              // v2.3+
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
                        .uri(ORD))
                // Export + GlobalDashboard -> core
                .route("core-export", r -> r
                        .path("/api/export/**", "/api/global-dashboard/**")
                        .uri(CORE))
                // System overview -> core
                .route("core-system", r -> r
                        .path("/api/system/**")
                        .uri(CORE))
                // Data dashboard -> core
                .route("core-dashboard", r -> r
                        .path("/api/dashboard/**")
                        .uri(CORE))
                // Inventory -> inventory service
                .route("inventory-api", r -> r
                        .path("/api/warehouses/**", "/api/inventory/**")
                        .uri(INV))
                // Payment -> payment service
                .route("payment-api", r -> r
                        .path("/api/payment/**")
                        .uri(PAY))
                // Password -> core
                .route("core-password", r -> r
                        .path("/api/password/**")
                        .uri(CORE))
                // Business API -> core
                .route("core-api", r -> r
                        .path("/api/**")
                        .uri(CORE))
                // SPA -> core
                .route("core-spa", r -> r
                        .path("/**")
                        .uri(CORE))
                .build();
    }
}

package com.yunxingcloud.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth endpoints -> core
                .route("core-auth", r -> r
                        .path("/api/login", "/api/refresh", "/api/logout", "/api/csrf", "/api/captcha", "/api/publicKey")
                        .uri("lb://yunxingcloud-core"))
                // User registration -> usercenter
                .route("usercenter-register", r -> r
                        .path("/api/register")
                        .uri("lb://yunxingcloud-usercenter"))
                // OAuth2 social login -> usercenter
                .route("usercenter-oauth2", r -> r
                        .path("/oauth2/**", "/login/**")
                        .uri("lb://yunxingcloud-usercenter"))
                // OIDC discovery -> usercenter
                .route("usercenter-wellknown", r -> r
                        .path("/.well-known/**")
                        .uri("lb://yunxingcloud-usercenter"))
                // Roles -> core
                .route("core-roles", r -> r
                        .path("/api/roles/**")
                        .uri("lb://yunxingcloud-core"))
                // Departments -> core
                .route("core-depts", r -> r
                        .path("/api/departments/**")
                        .uri("lb://yunxingcloud-core"))
                // Business API -> core
                .route("core-api", r -> r
                        .path("/api/**")
                        .uri("lb://yunxingcloud-core"))
                // SPA frontend -> core
                .route("core-spa", r -> r
                        .path("/**")
                        .uri("lb://yunxingcloud-core"))
                .build();
    }
}

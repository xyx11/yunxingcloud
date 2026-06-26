package com.yunxingcloud.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private static final String CORE = "http://localhost:8080";
    private static final String UC = "http://localhost:8081";

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
                // User management -> usercenter (social accounts only)
                .route("usercenter-users", r -> r
                        .path("/api/users/**", "/api/user/social/**")
                        .uri(UC))
                // Roles -> usercenter
                .route("usercenter-roles", r -> r
                        .path("/api/roles/**")
                        .uri(UC))
                // Departments -> usercenter
                .route("usercenter-depts", r -> r
                        .path("/api/departments/**")
                        .uri(UC))
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

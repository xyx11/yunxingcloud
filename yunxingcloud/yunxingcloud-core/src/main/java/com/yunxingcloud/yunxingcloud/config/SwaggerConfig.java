package com.yunxingcloud.yunxingcloud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("yunxingcloud API")
                        .version("2.3")
                        .description("分布式微服务平台 — SSO 认证、系统管理、订单、支付、库存全链路 API")
                        .contact(new Contact().name("yunxingcloud").email("admin@yunxingcloud.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .schemaRequirement("Bearer", new SecurityScheme()
                        .name("Bearer")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder().group("认证授权")
                .pathsToMatch("/api/login", "/api/refresh", "/api/logout", "/api/captcha",
                        "/api/password/**", "/api/register", "/api/csrf", "/api/publicKey")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("系统管理")
                .pathsToMatch("/api/users/**", "/api/roles/**", "/api/menus/**",
                        "/api/departments/**", "/api/posts/**", "/api/dict/**",
                        "/api/config/**", "/api/notices/**", "/api/messages/**")
                .build();
    }

    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder().group("监控运维")
                .pathsToMatch("/api/system/**", "/api/maintenance/**", "/api/dashboard/**",
                        "/api/oper-logs/**", "/api/login-logs/**", "/api/sys-logs/**",
                        "/api/stats/**", "/api/global-dashboard/**", "/api/realtime/**",
                        "/api/data-screen/**", "/api/online-users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder().group("业务管理")
                .pathsToMatch("/api/tickets/**", "/api/approvals/**", "/api/jobs/**",
                        "/api/ip-blacklist/**", "/api/emails/**", "/api/files/**",
                        "/api/export/**", "/api/search/**", "/api/oauth2-clients/**",
                        "/api/consent/**", "/api/activity-logs/**")
                .build();
    }
}
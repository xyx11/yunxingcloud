package com.yunxingcloud.yunxingcloud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("yunxingcloud API")
                        .version("1.0")
                        .description("yunxingcloud SSO 认证中心 + 系统管理 API"));
    }
}

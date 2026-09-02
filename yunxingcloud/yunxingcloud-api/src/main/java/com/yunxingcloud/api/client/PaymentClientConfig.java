package com.yunxingcloud.api.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

/**
 * order 服务内部直连 payment 服务时注入内部密钥头，
 * payment 的 JwtAuthFilter 识别该头放行（微服务间调用无需用户 JWT）
 */
public class PaymentClientConfig {

    public static final String INTERNAL_KEY = "yunxingcloud-internal";

    @Bean
    public RequestInterceptor internalKeyInterceptor() {
        return (RequestTemplate template) -> template.header("X-Internal-Key", INTERNAL_KEY);
    }
}

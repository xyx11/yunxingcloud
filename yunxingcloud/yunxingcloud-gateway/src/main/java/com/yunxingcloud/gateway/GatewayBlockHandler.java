package com.yunxingcloud.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@Configuration
public class GatewayBlockHandler {

    private static final String DEFAULT_MESSAGE = "请求过于频繁，请稍后再试";

    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return (exchange, ex) -> ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "success", false,
                        "message", DEFAULT_MESSAGE
                ));
    }
}
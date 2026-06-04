package com.yunxingcloud.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutes {

    private static final String SSO_URL = "http://127.0.0.1:8080";
    private static final String UC_URL = "http://127.0.0.1:8081";

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .path("/api/usercenter/", () -> RouterFunctions.route()
                .GET("/**", req -> proxy(req, UC_URL))
                .POST("/**", req -> proxy(req, UC_URL))
                .PUT("/**", req -> proxy(req, UC_URL))
                .DELETE("/**", req -> proxy(req, UC_URL))
                .build())
            .GET("/api/**", req -> proxy(req, SSO_URL))
            .POST("/api/**", req -> proxy(req, SSO_URL))
            .PUT("/api/**", req -> proxy(req, SSO_URL))
            .DELETE("/api/**", req -> proxy(req, SSO_URL))
            .GET("/login/**", req -> proxy(req, SSO_URL))
            .GET("/oauth2/**", req -> proxy(req, SSO_URL))
            .POST("/oauth2/**", req -> proxy(req, SSO_URL))
            .GET("/.well-known/**", req -> proxy(req, SSO_URL))
            .GET("/**", req -> proxy(req, SSO_URL))
            .build();
    }

    private static Mono<ServerResponse> proxy(ServerRequest request, String baseUrl) {
        String path = request.uri().getPath();
        String query = request.uri().getRawQuery();

        WebClient client = WebClient.create(baseUrl);
        return client.method(request.method())
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (query != null) uriBuilder.query(query);
                    return uriBuilder.build();
                })
                .headers(h -> request.headers().asHttpHeaders().forEach((k, v) -> {
                    if (!"Host".equalsIgnoreCase(k) && !"Content-Length".equalsIgnoreCase(k)) {
                        h.addAll(k, v);
                    }
                }))
                .body(request.bodyToMono(String.class).defaultIfEmpty(""), String.class)
                .exchangeToMono(resp -> ServerResponse.status(resp.statusCode())
                        .headers(h -> h.addAll(resp.headers().asHttpHeaders()))
                        .body(resp.bodyToMono(String.class), String.class));
    }
}

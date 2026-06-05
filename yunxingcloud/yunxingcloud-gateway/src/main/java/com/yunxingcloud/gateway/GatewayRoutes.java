package com.yunxingcloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class GatewayRoutes {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutes.class);
    private static final String SSO_URL = "http://127.0.0.1:8080";
    private static final String UC_URL = "http://127.0.0.1:8081";

    // IP 级别限流
    private final Map<String, long[]> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000;

    // 简单熔断: 连续失败 > 5 次 → 打开 30s
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long circuitOpenUntil = 0;
    private static final int FAILURE_THRESHOLD = 5;
    private static final long CIRCUIT_TIMEOUT_MS = 30_000;

    private final WebClient ssoClient = WebClient.builder()
            .baseUrl(SSO_URL)
            .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    private final WebClient ucClient = WebClient.builder()
            .baseUrl(UC_URL)
            .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .GET("/actuator/health", this::health)
            .path("/api/usercenter/", () -> RouterFunctions.route()
                .GET("/**", req -> proxy(req, ucClient))
                .POST("/**", req -> proxy(req, ucClient))
                .PUT("/**", req -> proxy(req, ucClient))
                .DELETE("/**", req -> proxy(req, ucClient))
                .build())
            .GET("/api/**", req -> proxy(req, ssoClient))
            .POST("/api/**", req -> proxy(req, ssoClient))
            .PUT("/api/**", req -> proxy(req, ssoClient))
            .DELETE("/api/**", req -> proxy(req, ssoClient))
            .GET("/login/**", req -> proxy(req, ssoClient))
            .GET("/oauth2/**", req -> proxy(req, ssoClient))
            .POST("/oauth2/**", req -> proxy(req, ssoClient))
            .GET("/.well-known/**", req -> proxy(req, ssoClient))
            .GET("/**", req -> proxy(req, ssoClient))
            .filter(this::logFilter)
            .build();
    }

    private Mono<ServerResponse> health(ServerRequest req) {
        boolean ssoUp = isCircuitClosed();
        return ServerResponse.ok().bodyValue(Map.of(
                "status", ssoUp ? "UP" : "DEGRADED",
                "service", "gateway",
                "rateLimit", MAX_REQUESTS + "/min",
                "circuitBreaker", ssoUp ? "CLOSED" : "OPEN"));
    }

    private boolean isCircuitClosed() { return System.currentTimeMillis() > circuitOpenUntil; }

    private Mono<ServerResponse> proxy(ServerRequest request, WebClient client) {
        String ip = request.remoteAddress().map(Object::toString).orElse("unknown");

        if (!rateCheck(ip)) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .bodyValue(Map.of("error", "rate limit exceeded"));
        }

        if (!isCircuitClosed()) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .bodyValue(Map.of("error", "circuit breaker open"));
        }

        long start = System.currentTimeMillis();
        String path = request.uri().getPath();
        String query = request.uri().getRawQuery();

        return client.method(request.method())
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (query != null) uriBuilder.query(query);
                    return uriBuilder.build();
                })
                .headers(h -> request.headers().asHttpHeaders().forEach((k, v) -> {
                    if (!"Host".equalsIgnoreCase(k) && !"Content-Length".equalsIgnoreCase(k))
                        h.addAll(k, v);
                }))
                .body(request.bodyToMono(String.class).defaultIfEmpty(""), String.class)
                .exchangeToMono(resp -> {
                    long elapsed = System.currentTimeMillis() - start;
                    if (resp.statusCode().is5xxServerError()) {
                        onFailure();
                    } else {
                        onSuccess();
                    }
                    log.info("[GW] {} {} → {} {} ({}ms)", ip, request.method(), resp.statusCode(), path, elapsed);
                    return ServerResponse.status(resp.statusCode())
                            .headers(h -> h.addAll(resp.headers().asHttpHeaders()))
                            .body(resp.bodyToMono(String.class), String.class);
                })
                .onErrorResume(e -> {
                    onFailure();
                    log.error("[GW] 代理错误: {} {}", path, e.getMessage());
                    return ServerResponse.status(HttpStatus.BAD_GATEWAY)
                            .bodyValue(Map.of("error", "service unavailable"));
                });
    }

    private void onSuccess() { failureCount.set(0); }
    private void onFailure() {
        if (failureCount.incrementAndGet() >= FAILURE_THRESHOLD) {
            circuitOpenUntil = System.currentTimeMillis() + CIRCUIT_TIMEOUT_MS;
            log.warn("[GW] 熔断器打开，{}s 后恢复", CIRCUIT_TIMEOUT_MS / 1000);
        }
    }

    private boolean rateCheck(String ip) {
        long now = System.currentTimeMillis();
        long[] record = rateLimitMap.computeIfAbsent(ip, k -> new long[]{now, 0});
        synchronized (record) {
            if (now - record[0] > WINDOW_MS) { record[0] = now; record[1] = 0; }
            if (record[1] >= MAX_REQUESTS) return false;
            record[1]++;
            return true;
        }
    }

    private Mono<ServerResponse> logFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request);
    }
}

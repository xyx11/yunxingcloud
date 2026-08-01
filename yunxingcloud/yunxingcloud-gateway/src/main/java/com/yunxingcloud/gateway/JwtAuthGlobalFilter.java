package com.yunxingcloud.gateway;

import com.yunxingcloud.common.core.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final List<String> PUBLIC_PATHS = List.of(
            // Auth
            "/api/login", "/api/register", "/api/captcha", "/api/csrf",
            "/api/publicKey", "/api/refresh", "/api/logout",
            "/.well-known/**", "/oauth2/**", "/login/**",
            // Password reset
            "/api/password/forgot", "/api/password/reset",
            // Payment callback (external gateways)
            "/api/payment/callback/**",
            // Internal inter-service
            "/api/orders/internal/**", "/api/inventory/order-out", "/api/inventory/order-back",
            // Health & monitoring
            "/actuator/health", "/actuator/prometheus",
            // API docs
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/doc.html",
            // Public content
            "/api/notices/latest",
            // SSE stream
            "/api/inventory/alerts/stream"
    );

    private static final List<String> GET_ONLY_PUBLIC_PATHS = List.of(
            "/api/products/**", "/api/categories/**", "/api/brands/**",
            "/api/banners/**", "/api/skus/**", "/api/home/**", "/api/search/**",
            "/api/recommend/**", "/api/reviews/**", "/api/articles/**",
            "/api/group-buy/**", "/api/flash-sale/**", "/api/aggregate/**", "/api/seo/**", "/api/chat/**"
    );

    private final SecretKey key;

    public JwtAuthGlobalFilter(@Value("${jwt.secret}") String secret) {
        this.key = JwtTokenProvider.createKey(secret);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "GET";

        if (isPublicPath(path, method)) return chain.filter(exchange);

        String token = extractToken(exchange);
        if (token == null) return unauthorized(exchange, "缺少认证令牌");

        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            if ("refresh".equals(claims.get("type", String.class)))
                return unauthorized(exchange, "刷新令牌不可用于API访问");
            exchange.getRequest().mutate()
                    .header("X-Auth-Username", claims.getSubject())
                    .header("X-Auth-Authorities", claims.get("authorities", String.class))
                    .build();
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "令牌无效或已过期");
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path, String method) {
        if (PUBLIC_PATHS.stream().anyMatch(p -> matcher.match(p, path))) return true;
        if ("GET".equalsIgnoreCase(method))
            return GET_ONLY_PUBLIC_PATHS.stream().anyMatch(p -> matcher.match(p, path));
        return false;
    }

    private String extractToken(ServerWebExchange exchange) {
        String bearer = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = mapper.writeValueAsBytes(Map.of("success", false, "message", message));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() { return -100; }
}

package com.yunxingcloud.common.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Shared JWT token parsing — used by gateway/core/payment/order/inventory modules.
 */
public final class JwtTokenProvider {

    private JwtTokenProvider() {}

    public static SecretKey createKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public static Claims parseToken(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    public static String getUsername(Claims claims) {
        return claims.getSubject();
    }

    public static List<String> getAuthorities(Claims claims) {
        String authStr = claims.get("authorities", String.class);
        if (authStr == null || authStr.isBlank()) return List.of();
        return Arrays.stream(authStr.split(","))
                .filter(s -> !s.isBlank()).toList();
    }
}

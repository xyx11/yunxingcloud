package com.yunxingcloud.order.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final SecretKey key;

    public JwtAuthFilter(@Value("${jwt.secret:yunxingcloud-jwt-secret-key-2024-very-long-secret-key-min-256-bits}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                String username = claims.getSubject();
                String authStr = claims.get("authorities", String.class);
                List<SimpleGrantedAuthority> authorities = (authStr != null && !authStr.isEmpty())
                        ? Arrays.stream(authStr.split(",")).filter(s -> !s.isBlank()).map(SimpleGrantedAuthority::new).toList()
                        : List.of();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, authorities));
            } catch (Exception e) { log.debug("JWT parse skipped: {}", e.getMessage()); }
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) return bearer.substring(7);
        return null;
    }
}

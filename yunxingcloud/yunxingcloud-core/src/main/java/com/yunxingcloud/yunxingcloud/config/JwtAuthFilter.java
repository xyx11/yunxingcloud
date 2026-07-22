package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.common.core.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final SecretKey key;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = JwtTokenProvider.createKey(secret);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = JwtTokenProvider.extractToken(request);
        if (token != null) {
            try {
                Claims claims = JwtTokenProvider.parseToken(token, key);
                String username = JwtTokenProvider.getUsername(claims);
                List<String> auths = JwtTokenProvider.getAuthorities(claims);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null,
                                auths.stream().map(SimpleGrantedAuthority::new).toList()));
            } catch (Exception e) { log.debug("JWT parse skipped: {}", e.getMessage()); }
        }
        chain.doFilter(request, response);
    }
}

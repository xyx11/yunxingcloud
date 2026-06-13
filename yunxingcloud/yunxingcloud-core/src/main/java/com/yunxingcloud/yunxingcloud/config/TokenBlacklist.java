package com.yunxingcloud.yunxingcloud.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private static final String PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecretKey secretKey;
    private final Map<String, Long> fallback = new ConcurrentHashMap<>();

    public TokenBlacklist(SecretKey secretKey, @org.springframework.beans.factory.annotation.Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.secretKey = secretKey;
        this.redisTemplate = redisTemplate;
    }

    public void add(String token) {
        try {
            Date expiration = parseExpiration(token);
            if (expiration != null) {
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(ttl));
                }
            }
        } catch (Exception e) {
            try {
                Date expiration = parseExpiration(token);
                if (expiration != null) fallback.put(token, expiration.getTime());
            } catch (Exception ignored) {}
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (Exception e) {
            return fallback.containsKey(token);
        }
    }

    private Date parseExpiration(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.getExpiration();
    }
}

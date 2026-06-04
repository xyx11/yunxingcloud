package com.yunxingcloud.yunxingcloud.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final SecretKey secretKey;

    public TokenBlacklist(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void add(String token) {
        try {
            Date expiration = parseExpiration(token);
            if (expiration != null) {
                blacklist.put(token, expiration.getTime());
            }
        } catch (Exception ignored) {}
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }

    private Date parseExpiration(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.getExpiration();
    }
}

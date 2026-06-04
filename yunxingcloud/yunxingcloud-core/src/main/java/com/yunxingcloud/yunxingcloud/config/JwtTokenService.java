package com.yunxingcloud.yunxingcloud.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;
    private TokenBlacklist blacklist;

    public static final long ACCESS_EXPIRATION = 2 * 60 * 60;        // 2 hours
    public static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60;  // 7 days

    public JwtTokenService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setBlacklist(TokenBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    public String createAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION * 1000))
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            if (blacklist != null && blacklist.isBlacklisted(token)) {
                return false;
            }
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

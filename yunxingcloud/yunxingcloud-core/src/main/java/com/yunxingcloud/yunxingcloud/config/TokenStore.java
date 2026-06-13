package com.yunxingcloud.yunxingcloud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class TokenStore {

    private static final String REDIS_KEY = "token:store";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, TokenInfo> fallback = new ConcurrentHashMap<>();

    public TokenStore(@Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(String token, String username, long expiresAt) {
        TokenInfo info = new TokenInfo(username, Instant.now(), Instant.ofEpochMilli(expiresAt), Instant.now());
        try {
            redisTemplate.opsForHash().put(REDIS_KEY, token, info);
        } catch (Exception e) {
            fallback.put(token, info);
        }
    }

    public void touch(String token) {
        try {
            Object obj = redisTemplate.opsForHash().get(REDIS_KEY, token);
            if (obj instanceof TokenInfo ti) {
                redisTemplate.opsForHash().put(REDIS_KEY, token,
                    new TokenInfo(ti.username, ti.createdAt, ti.expiresAt, Instant.now()));
            }
        } catch (Exception e) {
            TokenInfo ti = fallback.get(token);
            if (ti != null) {
                fallback.put(token, new TokenInfo(ti.username, ti.createdAt, ti.expiresAt, Instant.now()));
            }
        }
    }

    public void remove(String token) {
        try {
            redisTemplate.opsForHash().delete(REDIS_KEY, token);
        } catch (Exception e) {
            fallback.remove(token);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> activeSessions() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);
            return entries.values().stream()
                    .filter(v -> v instanceof TokenInfo)
                    .map(v -> (TokenInfo) v)
                    .filter(t -> Instant.now().isBefore(t.expiresAt))
                    .map(t -> Map.<String, Object>of(
                            "token", t.username + "-" + t.createdAt.toEpochMilli(),
                            "username", t.username,
                            "createdAt", t.createdAt.toString(),
                            "expiresAt", t.expiresAt.toString(),
                            "lastAccessTime", t.lastAccessTime.toString()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return fallback.entrySet().stream()
                    .filter(entry -> Instant.now().isBefore(entry.getValue().expiresAt))
                    .map(entry -> Map.<String, Object>of(
                            "token", entry.getKey().substring(0, 16) + "...",
                            "username", entry.getValue().username,
                            "createdAt", entry.getValue().createdAt.toString(),
                            "expiresAt", entry.getValue().expiresAt.toString(),
                            "lastAccessTime", entry.getValue().lastAccessTime.toString()))
                    .collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public void revokeByUser(String username) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);
            for (Object key : entries.keySet()) {
                Object val = entries.get(key);
                if (val instanceof TokenInfo && ((TokenInfo) val).username.equals(username)) {
                    redisTemplate.opsForHash().delete(REDIS_KEY, key);
                }
            }
        } catch (Exception ex) {
            fallback.entrySet().removeIf(entry -> entry.getValue().username.equals(username));
        }
    }

    public int count() {
        try {
            Long size = redisTemplate.opsForHash().size(REDIS_KEY);
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            return fallback.size();
        }
    }

    record TokenInfo(String username, Instant createdAt, Instant expiresAt, Instant lastAccessTime) {}
}

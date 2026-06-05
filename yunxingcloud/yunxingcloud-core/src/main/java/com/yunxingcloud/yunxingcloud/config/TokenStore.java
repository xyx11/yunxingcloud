package com.yunxingcloud.yunxingcloud.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class TokenStore {

    private final Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();

    public void add(String token, String username, long expiresAt) {
        activeTokens.put(token, new TokenInfo(username, Instant.now(), Instant.ofEpochMilli(expiresAt)));
    }

    public void remove(String token) {
        activeTokens.remove(token);
    }

    public List<Map<String, Object>> activeSessions() {
        return activeTokens.entrySet().stream()
                .filter(e -> Instant.now().isBefore(e.getValue().expiresAt))
                .map(e -> Map.<String, Object>of(
                        "token", e.getKey().substring(0, 16) + "...",
                        "username", e.getValue().username,
                        "createdAt", e.getValue().createdAt.toString(),
                        "expiresAt", e.getValue().expiresAt.toString()))
                .collect(Collectors.toList());
    }

    public void revokeByUser(String username) {
        activeTokens.entrySet().removeIf(e -> e.getValue().username.equals(username));
    }

    public int count() { return activeTokens.size(); }

    record TokenInfo(String username, Instant createdAt, Instant expiresAt) {}
}

package com.yunxingcloud.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchSuggestService {

    private static final Logger log = LoggerFactory.getLogger(SearchSuggestService.class);
    private final JdbcTemplate jdbc;

    public SearchSuggestService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> suggest(String q, int limit) {
        if (q.isBlank()) return List.of();
        return jdbc.queryForList(
            "SELECT id, name, price, sales FROM product WHERE status='0' AND name LIKE ? ORDER BY sales DESC LIMIT ?",
            "%" + q.trim() + "%", limit);
    }

    public List<String> hotKeywords() {
        try {
            return jdbc.queryForList(
                "SELECT keyword FROM search_log GROUP BY keyword ORDER BY COUNT(*) DESC LIMIT 8",
                String.class);
        } catch (Exception e) {
            return List.of("iPhone 17", "MacBook Pro", "华为Mate 70", "茅台飞天", "Nike Dunk", "戴森V16", "空调", "运动鞋");
        }
    }

    public void logSearch(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            try { jdbc.update("INSERT INTO search_log (keyword, created_at) VALUES (?, NOW())", keyword.trim()); }
            catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
        }
    }

    public List<Map<String, Object>> complete(String q) {
        if (q.isBlank()) return List.of();
        return jdbc.queryForList(
            "SELECT DISTINCT name FROM product WHERE status='0' AND name LIKE ? LIMIT 8",
            q.trim() + "%");
    }
}

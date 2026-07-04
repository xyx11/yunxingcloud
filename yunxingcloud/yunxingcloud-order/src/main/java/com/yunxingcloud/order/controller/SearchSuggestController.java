package com.yunxingcloud.order.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchSuggestController {

    private final JdbcTemplate jdbc;

    public SearchSuggestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Search suggestions (auto-complete)
     */
    @GetMapping("/suggest")
    public List<Map<String, Object>> suggest(@RequestParam String q, @RequestParam(defaultValue = "5") int limit) {
        if (q.isBlank()) return List.of();
        String pattern = "%" + q.trim() + "%";
        return jdbc.queryForList(
            "SELECT id, name, price, sales FROM product WHERE status='0' AND name LIKE ? ORDER BY sales DESC LIMIT ?",
            pattern, limit
        );
    }

    /**
     * Hot search keywords
     */
    @GetMapping("/hot-keywords")
    public List<String> hotKeywords() {
        // Return top searched keywords from a search_log table, or fallback to static list
        try {
            return jdbc.queryForList(
                "SELECT keyword FROM search_log GROUP BY keyword ORDER BY COUNT(*) DESC LIMIT 8",
                String.class
            );
        } catch (Exception e) {
            return List.of("iPhone 17", "MacBook Pro", "华为Mate 70", "茅台飞天", "Nike Dunk", "戴森V16", "空调", "运动鞋");
        }
    }

    /**
     * Record search keyword
     */
    @PostMapping("/log")
    public Map<String, Object> logSearch(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword != null && !keyword.isBlank()) {
            try {
                jdbc.update("INSERT INTO search_log (keyword, created_at) VALUES (?, NOW())", keyword.trim());
            } catch (Exception ignored) {}
        }
        return Map.of("success", true);
    }

    /**
     * Search suggestions with prices (for autocomplete dropdown)
     */
    @GetMapping("/complete")
    public List<Map<String, Object>> complete(@RequestParam String q) {
        if (q.isBlank()) return List.of();
        String pattern = q.trim() + "%";
        return jdbc.queryForList(
            "SELECT DISTINCT name FROM product WHERE status='0' AND name LIKE ? LIMIT 8",
            pattern
        );
    }
}

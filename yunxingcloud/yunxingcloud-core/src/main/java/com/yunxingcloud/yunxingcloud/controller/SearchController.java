package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final JdbcTemplate jdbc;

    public SearchController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        String kw = "%" + q + "%";
        Map<String, Object> results = new LinkedHashMap<>();

        try {
            results.put("users", jdbc.queryForList(
                "SELECT id, username, nickname, email FROM users WHERE username LIKE ? OR nickname LIKE ? OR email LIKE ? LIMIT 5",
                kw, kw, kw));
        } catch (Exception ignored) {}

        try {
            results.put("menus", jdbc.queryForList(
                "SELECT id, name, path FROM sys_menu WHERE name LIKE ? OR path LIKE ? LIMIT 5", kw, kw));
        } catch (Exception ignored) {}

        try {
            results.put("roles", jdbc.queryForList(
                "SELECT id, name, code FROM role WHERE name LIKE ? OR code LIKE ? LIMIT 5", kw, kw));
        } catch (Exception ignored) {}

        try {
            results.put("configs", jdbc.queryForList(
                "SELECT id, name, config_key FROM sys_config WHERE name LIKE ? OR config_key LIKE ? LIMIT 5", kw, kw));
        } catch (Exception ignored) {}

        return ResponseEntity.ok(results);
    }
}

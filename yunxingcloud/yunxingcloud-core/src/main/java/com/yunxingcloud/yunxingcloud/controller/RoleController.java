package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final JdbcTemplate jdbcTemplate;

    public RoleController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> roles = jdbcTemplate.queryForList("SELECT * FROM role");
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(
                "SELECT * FROM role WHERE id = ?", id);
        if (roles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roles.get(0));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role WHERE code = ?", Integer.class, code);
        if (count != null && count > 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "角色编码已存在"));
        }
        jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions, enabled) VALUES (?,?,?,?,?)",
                body.get("name"), code,
                body.getOrDefault("description", ""),
                body.getOrDefault("permissions", ""),
                body.getOrDefault("enabled", true));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int updated = jdbcTemplate.update(
                "UPDATE role SET name=?, code=?, description=?, permissions=?, enabled=? WHERE id=?",
                body.get("name"), body.get("code"),
                body.getOrDefault("description", ""),
                body.getOrDefault("permissions", ""),
                body.getOrDefault("enabled", true),
                id);
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM role WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

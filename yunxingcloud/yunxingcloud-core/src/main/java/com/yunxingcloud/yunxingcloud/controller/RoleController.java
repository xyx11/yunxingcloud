package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final JdbcTemplate jdbc;

    public RoleController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private Map<String, Object> lowerKeys(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        row.forEach((k, v) -> m.put(k.toLowerCase(), v));
        return m;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> roles = jdbc.queryForList(
            "SELECT id, name, code, description, permissions, enabled, created_at FROM role ORDER BY id");
        return ResponseEntity.ok(roles.stream().map(this::lowerKeys).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM role WHERE id = ?", id);
        return rows.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rows.get(0));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        String name = body.get("name"), code = body.get("code");
        if (name == null || code == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "名称和编码不能为空"));
        jdbc.update("INSERT INTO role (name, code, description, permissions) VALUES (?,?,?,?)",
                name, code, body.getOrDefault("description", ""), body.getOrDefault("permissions", ""));
        return ResponseEntity.ok(Map.of("success", true, "message", "创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        jdbc.update("UPDATE role SET name=?, code=?, description=?, permissions=? WHERE id=?",
                body.get("name"), body.get("code"), body.getOrDefault("description", ""), body.getOrDefault("permissions", ""), id);
        return ResponseEntity.ok(Map.of("success", true, "message", "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbc.update("DELETE FROM user_roles WHERE role_id = ?", id);
        jdbc.update("DELETE FROM role WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final JdbcTemplate jdbcTemplate;
    private final I18nService i18n;

    public RoleController(JdbcTemplate jdbcTemplate, I18nService i18n) {
        this.jdbcTemplate = jdbcTemplate;
        this.i18n = i18n;
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

    @PreAuthorize("hasAuthority('role:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role WHERE code = ?", Integer.class, code);
        if (count != null && count > 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", i18n.msg("role.code_exists")));
        }
        jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions, enabled) VALUES (?,?,?,?,?)",
                body.get("name"), code,
                body.getOrDefault("description", ""),
                body.getOrDefault("permissions", ""),
                body.getOrDefault("enabled", true));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('role:write')")
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

    @PreAuthorize("hasAuthority('role:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM role WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

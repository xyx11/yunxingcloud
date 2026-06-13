package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> tree() {
        List<Map<String, Object>> all = jdbcTemplate.queryForList(
                "SELECT * FROM department WHERE enabled = TRUE ORDER BY sort_order");
        Map<Long, Map<String, Object>> map = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Map<String, Object> dept : all) {
            dept.put("children", new ArrayList<>());
            map.put(((Number) dept.get("id")).longValue(), dept);
        }
        for (Map<String, Object> dept : all) {
            Number parentId = (Number) dept.get("parent_id");
            if (parentId == null) {
                roots.add(dept);
            } else {
                Map<String, Object> parent = map.get(parentId.longValue());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(dept);
                }
            }
        }
        return ResponseEntity.ok(roots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        List<Map<String, Object>> depts = jdbcTemplate.queryForList(
                "SELECT * FROM department WHERE id = ?", id);
        if (depts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(depts.get(0));
    }

    @PreAuthorize("hasAuthority('dept:write')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update(
                "INSERT INTO department (name, parent_id, sort_order, enabled) VALUES (?,?,?,?)",
                body.get("name"),
                body.get("parentId"),
                body.getOrDefault("sortOrder", 0),
                body.getOrDefault("enabled", true));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('dept:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int updated = jdbcTemplate.update(
                "UPDATE department SET name=?, parent_id=?, sort_order=?, enabled=? WHERE id=?",
                body.get("name"),
                body.get("parentId"),
                body.getOrDefault("sortOrder", 0),
                body.getOrDefault("enabled", true),
                id);
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('dept:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM department WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<Map<String, Object>>> users(@PathVariable Long id) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, nickname FROM users WHERE department_id = ?", id);
        return ResponseEntity.ok(users);
    }
}

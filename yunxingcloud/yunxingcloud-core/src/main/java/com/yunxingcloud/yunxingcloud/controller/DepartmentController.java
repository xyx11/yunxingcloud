package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final JdbcTemplate jdbc;

    public DepartmentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> tree() {
        List<Map<String, Object>> all = jdbc.queryForList("SELECT id AS id, name AS name, parent_id AS parentId, sort_order AS sortOrder, enabled AS enabled FROM department ORDER BY sort_order");
        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> d : all) {
            d.put("children", new ArrayList<>());
            map.put(((Number) d.get("id")).longValue(), d);
        }
        for (Map<String, Object> d : all) {
            Number pid = (Number) d.get("parentId");
            if (pid == null || pid.longValue() == 0) roots.add(d);
            else { Map<String, Object> p = map.get(pid.longValue()); if (p != null) ((List<Map<String, Object>>) p.get("children")).add(d); }
        }
        return ResponseEntity.ok(roots);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "名称不能为空"));
        jdbc.update("INSERT INTO department (name, parent_id, sort_order) VALUES (?,?,?)",
                name, body.getOrDefault("parentId", null), body.getOrDefault("sortOrder", 0));
        return ResponseEntity.ok(Map.of("success", true, "message", "创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        jdbc.update("UPDATE department SET name=?, parent_id=?, sort_order=? WHERE id=?",
                body.get("name"), body.getOrDefault("parentId", null), body.getOrDefault("sortOrder", 0), id);
        return ResponseEntity.ok(Map.of("success", true, "message", "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbc.update("UPDATE users SET department_id = null WHERE department_id = ?", id);
        jdbc.update("DELETE FROM department WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
    }
}

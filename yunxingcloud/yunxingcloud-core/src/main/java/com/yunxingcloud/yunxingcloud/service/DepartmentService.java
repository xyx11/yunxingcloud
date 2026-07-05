package com.yunxingcloud.yunxingcloud.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DepartmentService {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> tree() {
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
        return roots;
    }

    public Optional<Map<String, Object>> getById(Long id) {
        List<Map<String, Object>> depts = jdbcTemplate.queryForList(
                "SELECT * FROM department WHERE id = ?", id);
        return depts.isEmpty() ? Optional.empty() : Optional.of(depts.get(0));
    }

    @Transactional
    public void create(Map<String, Object> body) {
        jdbcTemplate.update(
                "INSERT INTO department (name, parent_id, sort_order, enabled) VALUES (?,?,?,?)",
                body.get("name"),
                body.get("parentId"),
                body.getOrDefault("sortOrder", 0),
                body.getOrDefault("enabled", true));
    }

    @Transactional
    public boolean update(Long id, Map<String, Object> body) {
        int updated = jdbcTemplate.update(
                "UPDATE department SET name=?, parent_id=?, sort_order=?, enabled=? WHERE id=?",
                body.get("name"),
                body.get("parentId"),
                body.getOrDefault("sortOrder", 0),
                body.getOrDefault("enabled", true),
                id);
        return updated > 0;
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM department WHERE id = ?", id);
    }

    public List<Map<String, Object>> users(Long id) {
        return jdbcTemplate.queryForList(
                "SELECT id, username, nickname FROM users WHERE department_id = ?", id);
    }
}

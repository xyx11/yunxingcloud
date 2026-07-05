package com.yunxingcloud.yunxingcloud.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RoleService {

    private final JdbcTemplate jdbcTemplate;

    public RoleService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList("SELECT * FROM role");
    }

    public List<Map<String, Object>> getById(Long id) {
        return jdbcTemplate.queryForList("SELECT * FROM role WHERE id = ?", id);
    }

    @Transactional
    public void create(Map<String, Object> body) {
        String code = (String) body.get("code");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role WHERE code = ?", Integer.class, code);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("role.code_exists");
        }
        jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions, enabled) VALUES (?,?,?,?,?)",
                body.get("name"), code,
                body.getOrDefault("description", ""),
                body.getOrDefault("permissions", ""),
                body.getOrDefault("enabled", true));
    }

    @Transactional
    public boolean update(Long id, Map<String, Object> body) {
        int updated = jdbcTemplate.update(
                "UPDATE role SET name=?, code=?, description=?, permissions=?, enabled=? WHERE id=?",
                body.get("name"), body.get("code"),
                body.getOrDefault("description", ""),
                body.getOrDefault("permissions", ""),
                body.getOrDefault("enabled", true),
                id);
        return updated > 0;
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM role WHERE id = ?", id);
    }
}

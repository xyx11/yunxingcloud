package com.yunxingcloud.yunxingcloud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void shouldBuildDepartmentTree() {
        var root = new HashMap<String, Object>();
        root.put("id", 1);
        root.put("parent_id", null);
        root.put("enabled", true);

        var child = new HashMap<String, Object>();
        child.put("id", 2);
        child.put("parent_id", 1);
        child.put("enabled", true);

        var child2 = new HashMap<String, Object>();
        child2.put("id", 3);
        child2.put("parent_id", 1);
        child2.put("enabled", true);

        when(jdbcTemplate.queryForList("SELECT * FROM department WHERE enabled = TRUE ORDER BY sort_order"))
                .thenReturn(List.of(root, child, child2));

        var tree = departmentService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).get("id")).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) tree.get(0).get("children");
        assertThat(children).hasSize(2);
        verify(jdbcTemplate).queryForList("SELECT * FROM department WHERE enabled = TRUE ORDER BY sort_order");
    }

    @Test
    void shouldGetDepartmentById() {
        var dept = new HashMap<String, Object>();
        dept.put("id", 1L);
        dept.put("name", "Engineering");

        when(jdbcTemplate.queryForList("SELECT * FROM department WHERE id = ?", 1L))
                .thenReturn(List.of(dept));

        var result = departmentService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().get("id")).isEqualTo(1L);
        assertThat(result.get().get("name")).isEqualTo("Engineering");
        verify(jdbcTemplate).queryForList("SELECT * FROM department WHERE id = ?", 1L);
    }

    @Test
    void shouldReturnEmptyWhenDepartmentNotFound() {
        when(jdbcTemplate.queryForList("SELECT * FROM department WHERE id = ?", 99L))
                .thenReturn(List.of());

        var result = departmentService.getById(99L);

        assertThat(result).isEmpty();
        verify(jdbcTemplate).queryForList("SELECT * FROM department WHERE id = ?", 99L);
    }

    @Test
    void shouldCreateDepartment() {
        var body = new HashMap<String, Object>();
        body.put("name", "QA");
        body.put("parentId", 1);
        body.put("sortOrder", 10);
        body.put("enabled", true);

        when(jdbcTemplate.update(
                "INSERT INTO department (name, parent_id, sort_order, enabled) VALUES (?,?,?,?)",
                "QA", 1, 10, true)).thenReturn(1);

        departmentService.create(body);

        verify(jdbcTemplate).update(
                "INSERT INTO department (name, parent_id, sort_order, enabled) VALUES (?,?,?,?)",
                "QA", 1, 10, true);
    }

    @Test
    void shouldUpdateDepartment() {
        var body = new HashMap<String, Object>();
        body.put("name", "Finance");
        body.put("parentId", null);
        body.put("sortOrder", 5);
        body.put("enabled", true);

        when(jdbcTemplate.update(
                "UPDATE department SET name=?, parent_id=?, sort_order=?, enabled=? WHERE id=?",
                "Finance", null, 5, true, 1L)).thenReturn(1);

        var result = departmentService.update(1L, body);

        assertThat(result).isTrue();
        verify(jdbcTemplate).update(
                "UPDATE department SET name=?, parent_id=?, sort_order=?, enabled=? WHERE id=?",
                "Finance", null, 5, true, 1L);
    }

    @Test
    void shouldDeleteDepartment() {
        departmentService.delete(1L);

        verify(jdbcTemplate).update("DELETE FROM department WHERE id = ?", 1L);
    }

    @Test
    void shouldListDepartmentUsers() {
        var user = new HashMap<String, Object>();
        user.put("id", 10L);
        user.put("username", "alice");
        user.put("nickname", "Alice");

        var user2 = new HashMap<String, Object>();
        user2.put("id", 11L);
        user2.put("username", "bob");
        user2.put("nickname", "Bob");

        when(jdbcTemplate.queryForList(
                "SELECT id, username, nickname FROM users WHERE department_id = ?", 1L))
                .thenReturn(List.of(user, user2));

        var users = departmentService.users(1L);

        assertThat(users).hasSize(2);
        assertThat(users.get(0).get("username")).isEqualTo("alice");
        assertThat(users.get(1).get("username")).isEqualTo("bob");
        verify(jdbcTemplate).queryForList(
                "SELECT id, username, nickname FROM users WHERE department_id = ?", 1L);
    }
}

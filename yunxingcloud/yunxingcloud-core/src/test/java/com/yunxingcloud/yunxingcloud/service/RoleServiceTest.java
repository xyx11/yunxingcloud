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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RoleService roleService;

    @Test
    void shouldListRoles() {
        List<Map<String, Object>> roles = List.of(Map.of(
                "id", 1L,
                "name", "admin",
                "code", "admin"
        ));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(roles);

        List<Map<String, Object>> result = roleService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("id", 1L)
                .containsEntry("name", "admin")
                .containsEntry("code", "admin");
        verify(jdbcTemplate).queryForList(anyString());
    }

    @Test
    void shouldGetRoleById() {
        List<Map<String, Object>> roles = List.of(Map.of(
                "id", 1L,
                "name", "admin",
                "code", "admin"
        ));
        when(jdbcTemplate.queryForList(anyString(), eq(1L))).thenReturn(roles);

        List<Map<String, Object>> result = roleService.getById(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("id", 1L)
                .containsEntry("name", "admin")
                .containsEntry("code", "admin");
        verify(jdbcTemplate).queryForList(anyString(), eq(1L));
    }

    @Test
    void shouldCreateRole() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Admin");
        body.put("code", "admin");
        body.put("description", "Administrator role");
        body.put("permissions", "all");
        body.put("enabled", true);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("admin"))).thenReturn(0);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(1);

        roleService.create(body);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq("admin"));
        verify(jdbcTemplate).update(anyString(),
                eq("Admin"), eq("admin"),
                eq("Administrator role"), eq("all"), eq(true));
    }

    @Test
    void shouldThrowWhenCreateDuplicateRoleCode() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Admin");
        body.put("code", "admin");

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("admin"))).thenReturn(1);

        assertThatThrownBy(() -> roleService.create(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("role.code_exists");

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldUpdateRole() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Admin Updated");
        body.put("code", "admin");
        body.put("description", "Updated description");
        body.put("permissions", "read,write");
        body.put("enabled", false);

        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        boolean result = roleService.update(1L, body);

        assertThat(result).isTrue();
        verify(jdbcTemplate).update(anyString(),
                eq("Admin Updated"), eq("admin"),
                eq("Updated description"), eq("read,write"), eq(false),
                eq(1L));
    }

    @Test
    void shouldDeleteRole() {
        roleService.delete(1L);

        verify(jdbcTemplate).update(anyString(), eq(1L));
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理", description = "角色CRUD")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final I18nService i18n;

    public RoleController(RoleService roleService, I18nService i18n) {
        this.roleService = roleService;
        this.i18n = i18n;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(roleService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        List<Map<String, Object>> roles = roleService.getById(id);
        if (roles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roles.get(0));
    }

    @PreAuthorize("hasAuthority('role:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            roleService.create(body);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", i18n.msg(e.getMessage())));
        }
    }

    @PreAuthorize("hasAuthority('role:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (roleService.update(id, body)) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('role:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

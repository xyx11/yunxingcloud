package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "用户角色", description = "用户中心角色查询")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final I18nService i18n;

    public RoleController(RoleRepository roleRepository, I18nService i18n) {
        this.roleRepository = roleRepository;
        this.i18n = i18n;
    }

    @Operation(summary = "查询角色列表")
    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<?> create(@RequestBody Role role) {
        if (roleRepository.existsByCode(role.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("role.code_exists")));
        }
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role body) {
        return roleRepository.findById(id).map(role -> {
            role.setName(body.getName());
            role.setCode(body.getCode());
            role.setDescription(body.getDescription());
            role.setPermissions(body.getPermissions());
            role.setEnabled(body.isEnabled());
            return ResponseEntity.ok(roleRepository.save(role));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

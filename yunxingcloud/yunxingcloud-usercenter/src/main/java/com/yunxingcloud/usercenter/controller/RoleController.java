package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Role role) {
        if (roleRepository.existsByCode(role.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "角色编码已存在"));
        }
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

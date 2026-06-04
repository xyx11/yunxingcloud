package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import com.yunxingcloud.usercenter.service.DeptRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserRepository userRepository;
    private final DeptRoleService deptRoleService;

    public UserManageController(UserRepository userRepository, DeptRoleService deptRoleService) {
        this.userRepository = userRepository;
        this.deptRoleService = deptRoleService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .<Map<String, Object>>map(u -> {
                    List<Map<String, Object>> roles = u.getRoles().stream()
                            .map(r -> Map.<String, Object>of("id", r.getId(), "name", r.getName(), "code", r.getCode()))
                            .toList();
                    return Map.<String, Object>of(
                            "id", u.getId(),
                            "username", u.getUsername(),
                            "nickname", u.getNickname() != null ? u.getNickname() : "",
                            "email", u.getEmail() != null ? u.getEmail() : "",
                            "departmentId", u.getDepartmentId() != null ? u.getDepartmentId() : 0,
                            "registerSource", u.getRegisterSource(),
                            "enabled", u.isEnabled(),
                            "roles", roles
                    );
                })
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/department")
    public ResponseEntity<Map<String, Object>> setDepartment(@PathVariable Long id,
                                                              @RequestBody Map<String, Long> body) {
        deptRoleService.setUserDepartment(id, body.get("departmentId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Map<String, Object>> setRoles(@PathVariable Long id,
                                                         @RequestBody Map<String, List<Long>> body) {
        deptRoleService.setUserRoles(id, body.getOrDefault("roleIds", List.of()));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<String>> permissions() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(deptRoleService.getUserPermissions(auth.getName()));
    }
}

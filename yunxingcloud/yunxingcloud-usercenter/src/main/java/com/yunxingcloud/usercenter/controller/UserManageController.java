package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import com.yunxingcloud.usercenter.service.DeptRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserRepository userRepository;
    private final DeptRoleService deptRoleService;
    private final PasswordEncoder passwordEncoder;

    public UserManageController(UserRepository userRepository,
                                 DeptRoleService deptRoleService,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deptRoleService = deptRoleService;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件为空"));
        }
        int success = 0, fail = 0;
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) { fail++; continue; }
                try {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String email = parts.length > 2 ? parts[2].trim() : "";
                    if (userRepository.existsByUsername(username)) { fail++; continue; }
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword(passwordEncoder.encode(password));
                    u.setEmail(email);
                    u.setRegisterSource("import");
                    userRepository.save(u);
                    success++;
                } catch (Exception e) { fail++; }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "解析失败: " + e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("success", true, "imported", success, "failed", fail));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "enabled", u.isEnabled()));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import com.yunxingcloud.usercenter.service.DeptRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tag(name = "用户管理", description = "用户CRUD管理")
@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserRepository userRepository;
    private final DeptRoleService deptRoleService;
    private final PasswordEncoder passwordEncoder;
    private final I18nService i18n;

    public UserManageController(UserRepository userRepository,
                                 DeptRoleService deptRoleService,
                                 PasswordEncoder passwordEncoder,
                                 I18nService i18n) {
        this.userRepository = userRepository;
        this.deptRoleService = deptRoleService;
        this.passwordEncoder = passwordEncoder;
        this.i18n = i18n;
    }

    @Operation(summary = "查询用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
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
    @PreAuthorize("hasAuthority('dept:write')")
    public ResponseEntity<Map<String, Object>> setDepartment(@PathVariable Long id,
                                                              @RequestBody Map<String, Long> body) {
        deptRoleService.setUserDepartment(id, body.get("departmentId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('role:write')")
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
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.empty")));
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
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.parse_failed") + ": " + e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("success", true, "imported", success, "failed", fail));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "enabled", u.isEnabled()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        String email = body.get("email");
        if (username == null || username.isBlank()) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("register.username_blank")));
        if (password == null || password.length() < 6) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("validate.password_min")));
        if (userRepository.existsByUsername(username)) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("register.duplicate_username")));
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setNickname(nickname);
        u.setEmail(email);
        u.setRegisterSource("manual");
        userRepository.save(u);
        return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.create_success")));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(u -> {
            if (body.containsKey("nickname")) u.setNickname(body.get("nickname"));
            if (body.containsKey("email")) u.setEmail(body.get("email"));
            userRepository.save(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.update_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reset-pwd")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.pwd_reset")));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(u -> Map.<String, Object>of(
                "id", u.getId(), "username", u.getUsername(), "nickname", u.getNickname() != null ? u.getNickname() : "",
                "email", u.getEmail() != null ? u.getEmail() : "", "departmentId", u.getDepartmentId() != null ? u.getDepartmentId() : 0,
                "registerSource", u.getRegisterSource(), "enabled", u.isEnabled(), "roles", List.of()
        )).toList());
    }

    @PutMapping("/{id}/department")
    public ResponseEntity<Map<String, Object>> setDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        userRepository.findById(id).ifPresent(u -> { u.setDepartmentId(body.get("departmentId")); userRepository.save(u); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Map<String, Object>> setRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> { u.setEnabled(!u.isEnabled()); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true, "enabled", u.isEnabled())); }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username"), password = body.get("password");
        if (username == null || username.isBlank()) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名不能为空"));
        if (password == null || password.length() < 6) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码至少6位"));
        if (userRepository.existsByUsername(username)) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名已存在"));
        User u = new User(username, passwordEncoder.encode(password), body.get("email"));
        if (body.containsKey("nickname")) u.setNickname(body.get("nickname"));
        u.setRegisterSource("manual"); userRepository.save(u);
        return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", "创建成功"));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(u -> { if (body.containsKey("nickname")) u.setNickname(body.get("nickname")); if (body.containsKey("email")) u.setEmail(body.get("email")); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true)); }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reset-pwd")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> { u.setPassword(passwordEncoder.encode("123456")); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", "密码已重置为 123456")); }).orElse(ResponseEntity.notFound().build());
    }
}

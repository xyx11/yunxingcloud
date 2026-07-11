package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "用户管理", description = "用户的增删改查、部门/角色分配、导入导出")
@Tag(name = "用户管理", description = "用户 CRUD、角色分配、部门关联")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;
    private final I18nService i18n;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbc,
                          I18nService i18n) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
        this.i18n = i18n;
    }

    private boolean isAdmin() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Long c = jdbc.queryForObject("SELECT COUNT(*) FROM user_roles ur JOIN role r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE u.username=? AND r.code='admin'", Long.class, name);
            return c != null && c > 0;
        } catch (Exception e) { return false; }
    }

    private Long getCurrentUserDeptId() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try { return jdbc.queryForObject("SELECT department_id FROM users WHERE username=?", Long.class, name); } catch (Exception e) { return null; }
    }

    private Set<Long> getDeptAndChildren(Long deptId) {
        Set<Long> ids = new LinkedHashSet<>();
        if (deptId == null) return ids;
        ids.add(deptId);
        try {
            List<Long> children = jdbc.queryForList("SELECT id FROM department WHERE parent_id=?", Long.class, deptId);
            for (Long child : children) ids.addAll(getDeptAndChildren(child));
        } catch (Exception ignored) {}
        return ids;
    }

    @Operation(summary = "查询用户列表")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<Long, String> deptNames = new HashMap<>();
        try { jdbc.queryForList("SELECT id, name FROM department").forEach(r -> deptNames.put(((Number)r.get("id")).longValue(), (String)r.get("name"))); } catch (Exception ignored) {}
        Map<Long, List<Map<String, Object>>> userRoles = new HashMap<>();
        try {
            jdbc.queryForList("SELECT ur.user_id, r.id, r.name, r.code FROM user_roles ur JOIN role r ON r.id = ur.role_id")
                .forEach(r -> {
                    Long uid = ((Number) r.get("user_id")).longValue();
                    userRoles.computeIfAbsent(uid, k -> new ArrayList<>())
                        .add(Map.of("id", ((Number) r.get("id")).longValue(), "name", (String) r.get("name"), "code", (String) r.get("code")));
                });
        } catch (Exception ignored) {}
        var pageable = PageRequest.of(page - 1, pageSize);
        var pageResult = (keyword != null && !keyword.isBlank())
            ? userRepository.findByUsernameContainingOrNicknameContainingOrEmailContaining(keyword, keyword, keyword, pageable)
            : userRepository.findAll(pageable);

        // 部门数据权限：非admin用户只能看到本部门及子部门的用户，无部门者仅见自己
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean admin = isAdmin();
        Set<Long> allowedDeptIds = admin ? null : getDeptAndChildren(getCurrentUserDeptId());

        var items = pageResult.stream()
            .filter(u -> admin || u.getUsername().equals(currentUser)
                || (allowedDeptIds != null && !allowedDeptIds.isEmpty()
                    && u.getDepartmentId() != null && allowedDeptIds.contains(u.getDepartmentId())))
            .map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname() != null ? u.getNickname() : "");
            m.put("email", u.getEmail() != null ? u.getEmail() : "");
            m.put("departmentId", u.getDepartmentId() != null ? u.getDepartmentId() : 0);
            m.put("postId", u.getPostId() != null ? u.getPostId() : 0);
            m.put("departmentName", deptNames.getOrDefault(u.getDepartmentId(), "-"));
            m.put("registerSource", u.getRegisterSource());
            m.put("enabled", u.isEnabled());
            m.put("approved", u.isApproved());
            m.put("lastLoginTime", u.getLastLoginTime() != null ? u.getLastLoginTime().toString() : "");
            m.put("roles", userRoles.getOrDefault(u.getId(), List.of()));
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("items", items, "total", pageResult.getTotalElements()));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/department")
    public ResponseEntity<Map<String, Object>> setDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        if (body.get("departmentId") == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("common.bad_request")));
        userRepository.findById(id).ifPresent(u -> { u.setDepartmentId(body.get("departmentId")); userRepository.save(u); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/post")
    public ResponseEntity<Map<String, Object>> setPost(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        if (body.get("postId") == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("common.bad_request")));
        userRepository.findById(id).ifPresent(u -> { u.setPostId(body.get("postId")); userRepository.save(u); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<Map<String, Object>> setRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        if (roleIds == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("user.role_ids_empty")));
        }
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
        for (Long roleId : roleIds) {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", id, roleId);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> { u.setEnabled(!u.isEnabled()); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true, "enabled", u.isEnabled())); }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username"), password = body.get("password");
        if (username == null || username.isBlank()) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("register.username_blank")));
        if (password == null || password.length() < 6) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("validate.password_min")));
        if (userRepository.existsByUsername(username)) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("register.duplicate_username")));
        User u = new User(username, passwordEncoder.encode(password), body.get("email"));
        if (body.containsKey("nickname")) u.setNickname(body.get("nickname"));
        u.setRegisterSource("manual"); userRepository.save(u);
        return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.create_success")));
    }

    @Operation(summary = "修改个人信息")
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(u -> { if (body.containsKey("nickname")) u.setNickname(body.get("nickname")); if (body.containsKey("email")) u.setEmail(body.get("email")); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true)); }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("user.ids_empty")));
        for (Long id : ids) { jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id); userRepository.deleteById(id); }
        return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("user.delete_batch", ids.size())));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = "用户名,密码,邮箱,昵称\nzhangsan,123456,zhangsan@example.com,张三\nlisi,123456,lisi@example.com,李四\n";
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=users_template.csv").header("Content-Type", "text/csv; charset=UTF-8").body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.empty")));
        int created = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 3) { skipped++; continue; }
                String username = cols[0].trim(), password = cols[1].trim(), email = cols.length > 2 ? cols[2].trim() : "";
                if (username.isEmpty() || password.isEmpty() || userRepository.existsByUsername(username)) { skipped++; continue; }
                User u = new User(username, passwordEncoder.encode(password), email);
                if (cols.length > 3 && !cols[3].trim().isEmpty()) u.setNickname(cols[3].trim());
                u.setRegisterSource("import");
                userRepository.save(u);
                created++;
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("user.import_failed", e.getMessage())));
        }
        return ResponseEntity.ok(Map.of("success", true, "created", created, "skipped", skipped, "message", i18n.msg("user.import_complete", created, skipped)));
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("user.delete_success")));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/{id}/reset-pwd")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> { u.setPassword(passwordEncoder.encode("123456")); userRepository.save(u); return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.pwd_reset"))); }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/pending-approval")
    public ResponseEntity<List<Map<String, Object>>> pendingApproval() {
        List<Map<String, Object>> list = userRepository.findByApproved(false).stream()
            .map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail() != null ? u.getEmail() : "",
                "registerSource", u.getRegisterSource()
            ))
            .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setApproved(true);
            userRepository.save(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.approve_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
            userRepository.delete(u);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.reject_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder sb = new StringBuilder("用户名,昵称,邮箱,来源,状态\n");
        userRepository.findAll().forEach(u -> sb.append(String.format("%s,%s,%s,%s,%s\n",
            u.getUsername(), u.getNickname() != null ? u.getNickname() : "",
            u.getEmail() != null ? u.getEmail() : "", u.getRegisterSource(),
            u.isEnabled() ? "正常" : "停用")));
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=users.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(bytes);
    }
}

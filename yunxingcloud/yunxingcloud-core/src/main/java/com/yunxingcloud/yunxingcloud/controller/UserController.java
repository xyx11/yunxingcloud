package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "用户管理", description = "用户的增删改查、部门/角色分配、导入导出")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询用户列表")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.listUsers(page, pageSize, keyword, currentUser));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/department")
    public ResponseEntity<Map<String, Object>> setDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        userService.setUserDepartment(id, body.get("departmentId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/post")
    public ResponseEntity<Map<String, Object>> setPost(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        userService.setUserPost(id, body.get("postId"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<Map<String, Object>> setRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        return ResponseEntity.ok(userService.setUserRoles(id, body.get("roleIds")));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        Map<String, Object> result = userService.toggleUserStatus(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        log.info("创建用户: username={}", body.get("username"));
        return ResponseEntity.ok(userService.createUser(body));
    }

    @Operation(summary = "修改个人信息")
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Map<String, Object> result = userService.updateUserProfile(id, body);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ids 不能为空"));
        }
        log.info("批量删除用户: ids={}", ids);
        return ResponseEntity.ok(userService.batchDeleteUsers(ids));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = "用户名,密码,邮箱,昵称\nzhangsan,123456,zhangsan@example.com,张三\nlisi,123456,lisi@example.com,李四\n";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users_template.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件不能为空"));
        }
        log.info("导入用户: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        return ResponseEntity.ok(userService.importUsers(file));
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        log.info("删除用户: id={}", id);
        Map<String, Object> result = userService.deleteUser(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/{id}/reset-pwd")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        Map<String, Object> result = userService.resetUserPassword(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/pending-approval")
    public ResponseEntity<List<Map<String, Object>>> pendingApproval() {
        return ResponseEntity.ok(userService.pendingApprovals());
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveUser(@PathVariable Long id) {
        Map<String, Object> result = userService.approveUser(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectUser(@PathVariable Long id) {
        Map<String, Object> result = userService.rejectUser(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        byte[] bytes = userService.exportUsers();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(bytes);
    }
}

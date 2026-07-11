package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.service.DeptRoleService;
import com.yunxingcloud.usercenter.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Tag(name = "用户管理", description = "用户CRUD管理")
@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserService userService;
    private final DeptRoleService deptRoleService;
    private final I18nService i18n;

    public UserManageController(UserService userService,
                                 DeptRoleService deptRoleService,
                                 I18nService i18n) {
        this.userService = userService;
        this.deptRoleService = deptRoleService;
        this.i18n = i18n;
    }

    @Operation(summary = "查询用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(userService.listUsers());
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
        return ResponseEntity.ok(userService.importUsers(file));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.createUser(body));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.updateUserProfile(id, body));
    }

    @PostMapping("/{id}/reset-pwd")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(userService.resetUserPassword(id));
    }
}

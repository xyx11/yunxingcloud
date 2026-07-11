package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.usercenter.entity.Department;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.DepartmentRepository;
import com.yunxingcloud.usercenter.service.DeptRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "用户部门", description = "用户中心部门查询")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final DeptRoleService deptRoleService;

    public DepartmentController(DepartmentRepository departmentRepository,
                                 DeptRoleService deptRoleService) {
        this.departmentRepository = departmentRepository;
        this.deptRoleService = deptRoleService;
    }

    @Operation(summary = "查询部门树")
    @GetMapping
    @PreAuthorize("hasAuthority('dept:read')")
    public ResponseEntity<List<Department>> tree() {
        return ResponseEntity.ok(deptRoleService.getDepartmentTree());
    }

    @Operation(summary = "查询部门详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:read')")
    public ResponseEntity<Department> getById(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('dept:write')")
    public ResponseEntity<Department> create(@RequestBody Department dept) {
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:write')")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Department body) {
        return departmentRepository.findById(id).map(dept -> {
            dept.setName(body.getName());
            dept.setParentId(body.getParentId());
            dept.setSortOrder(body.getSortOrder());
            dept.setEnabled(body.isEnabled());
            return ResponseEntity.ok(departmentRepository.save(dept));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:write')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('dept:read')")
    public ResponseEntity<List<Map<String, Object>>> users(@PathVariable Long id) {
        List<Map<String, Object>> users = deptRoleService.getDepartmentUsers(id).stream()
                .<Map<String, Object>>map(u -> Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "nickname", u.getNickname() != null ? u.getNickname() : ""
                ))
                .toList();
        return ResponseEntity.ok(users);
    }
}

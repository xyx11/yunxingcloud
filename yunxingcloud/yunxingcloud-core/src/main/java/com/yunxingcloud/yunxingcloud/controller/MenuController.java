package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "菜单管理", description = "系统菜单树 CRUD")
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) { this.menuService = menuService; }

    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/tree")
    public ResponseEntity<List<SysMenu>> tree() {
        return ResponseEntity.ok(menuService.tree());
    }

    @Operation(summary = "查询菜单列表")
    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping
    public ResponseEntity<List<SysMenu>> list() { return ResponseEntity.ok(menuService.list()); }

    @Operation(summary = "查询菜单详情")
    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysMenu> getById(@PathVariable Long id) {
        return menuService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增菜单")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('menu:write')")
    @PostMapping
    public ResponseEntity<SysMenu> create(@RequestBody SysMenu menu) {
        return ResponseEntity.ok(menuService.create(menu));
    }

    @Operation(summary = "修改菜单")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('menu:write')")
    @PutMapping("/{id}")
    public ResponseEntity<SysMenu> update(@PathVariable Long id, @RequestBody SysMenu body) {
        try {
            return ResponseEntity.ok(menuService.update(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除菜单")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('menu:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('menu:write')")
    @PutMapping("/{id}/move")
    public ResponseEntity<Map<String, Object>> move(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            menuService.move(id, body.getOrDefault("direction", 0));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

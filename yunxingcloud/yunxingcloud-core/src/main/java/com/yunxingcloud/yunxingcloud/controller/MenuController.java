package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
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

    private final SysMenuRepository menuRepository;

    public MenuController(SysMenuRepository menuRepository) { this.menuRepository = menuRepository; }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/tree")
    public ResponseEntity<List<SysMenu>> tree() {
        List<SysMenu> all = menuRepository.findByVisibleTrueOrderBySortOrder();
        Map<Long, SysMenu> map = new HashMap<>();
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : all) { map.put(m.getId(), m); }
        for (SysMenu m : all) {
            if (m.getParentId() == null) { roots.add(m); }
            else { SysMenu p = map.get(m.getParentId()); if (p != null) p.getChildren().add(m); }
        }
        return ResponseEntity.ok(roots);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping
    public ResponseEntity<List<SysMenu>> list() { return ResponseEntity.ok(menuRepository.findAll()); }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysMenu> getById(@PathVariable Long id) {
        return menuRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('menu:write')")
    @PostMapping
    public ResponseEntity<SysMenu> create(@RequestBody SysMenu menu) {
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('menu:write')")
    @PutMapping("/{id}")
    public ResponseEntity<SysMenu> update(@PathVariable Long id, @RequestBody SysMenu body) {
        return menuRepository.findById(id).map(m -> {
            m.setName(body.getName()); m.setParentId(body.getParentId());
            m.setSortOrder(body.getSortOrder()); m.setPath(body.getPath());
            m.setComponent(body.getComponent()); m.setIcon(body.getIcon());
            m.setMenuType(body.getMenuType()); m.setPerms(body.getPerms());
            m.setVisible(body.isVisible());
            return ResponseEntity.ok(menuRepository.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('menu:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        menuRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('menu:write')")
    @PutMapping("/{id}/move")
    public ResponseEntity<Map<String, Object>> move(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int direction = body.getOrDefault("direction", 0);
        return menuRepository.findById(id).map(m -> {
            List<SysMenu> siblings = m.getParentId() != null ? menuRepository.findByParentId(m.getParentId()) : menuRepository.findAll().stream().filter(s -> s.getParentId() == null).toList();
            siblings.sort((a, b) -> Integer.compare(a.getSortOrder() != null ? a.getSortOrder() : 0, b.getSortOrder() != null ? b.getSortOrder() : 0));
            int idx = -1;
            for (int i = 0; i < siblings.size(); i++) { if (siblings.get(i).getId().equals(m.getId())) { idx = i; break; } }
            int swapIdx = idx + direction;
            if (swapIdx >= 0 && swapIdx < siblings.size()) {
                SysMenu other = siblings.get(swapIdx);
                int tmp = m.getSortOrder() != null ? m.getSortOrder() : 0;
                m.setSortOrder(other.getSortOrder() != null ? other.getSortOrder() : 0);
                other.setSortOrder(tmp);
                menuRepository.save(m);
                menuRepository.save(other);
            }
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final SysMenuRepository menuRepository;

    public MenuController(SysMenuRepository menuRepository) { this.menuRepository = menuRepository; }

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

    @GetMapping
    public ResponseEntity<List<SysMenu>> list() { return ResponseEntity.ok(menuRepository.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<SysMenu> getById(@PathVariable Long id) {
        return menuRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysMenu> create(@RequestBody SysMenu menu) {
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        menuRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

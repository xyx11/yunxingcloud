package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MenuService {

    private final SysMenuRepository menuRepository;

    public MenuService(SysMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<SysMenu> tree() {
        List<SysMenu> all = menuRepository.findByVisibleTrueOrderBySortOrder();
        Map<Long, SysMenu> map = new HashMap<>();
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : all) { map.put(m.getId(), m); }
        for (SysMenu m : all) {
            if (m.getParentId() == null) { roots.add(m); }
            else { SysMenu p = map.get(m.getParentId()); if (p != null) p.getChildren().add(m); }
        }
        return roots;
    }

    public List<SysMenu> list() {
        return menuRepository.findAll();
    }

    public Optional<SysMenu> getById(Long id) {
        return menuRepository.findById(id);
    }

    @Transactional
    public SysMenu create(SysMenu menu) {
        return menuRepository.save(menu);
    }

    @Transactional
    public SysMenu update(Long id, SysMenu body) {
        return menuRepository.findById(id).map(m -> {
            m.setName(body.getName()); m.setParentId(body.getParentId());
            m.setSortOrder(body.getSortOrder()); m.setPath(body.getPath());
            m.setComponent(body.getComponent()); m.setIcon(body.getIcon());
            m.setMenuType(body.getMenuType()); m.setPerms(body.getPerms());
            m.setVisible(body.isVisible());
            return menuRepository.save(m);
        }).orElseThrow(() -> new IllegalArgumentException("Menu not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        menuRepository.deleteById(id);
    }

    @Transactional
    public void move(Long id, int direction) {
        SysMenu m = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + id));
        List<SysMenu> siblings = m.getParentId() != null
                ? menuRepository.findByParentId(m.getParentId())
                : menuRepository.findAll().stream().filter(s -> s.getParentId() == null).toList();
        siblings.sort(Comparator.comparingInt(a -> a.getSortOrder() != null ? a.getSortOrder() : 0));
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(m.getId())) { idx = i; break; }
        }
        int swapIdx = idx + direction;
        if (swapIdx >= 0 && swapIdx < siblings.size()) {
            SysMenu other = siblings.get(swapIdx);
            int tmp = m.getSortOrder() != null ? m.getSortOrder() : 0;
            m.setSortOrder(other.getSortOrder() != null ? other.getSortOrder() : 0);
            other.setSortOrder(tmp);
            menuRepository.save(m);
            menuRepository.save(other);
        }
    }
}

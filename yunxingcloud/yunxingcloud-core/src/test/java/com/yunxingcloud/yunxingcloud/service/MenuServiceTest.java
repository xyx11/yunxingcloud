package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private SysMenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void shouldBuildMenuTree() {
        var root = new SysMenu();
        root.setId(1L);
        root.setParentId(null);

        var child = new SysMenu();
        child.setId(2L);
        child.setParentId(1L);

        var child2 = new SysMenu();
        child2.setId(3L);
        child2.setParentId(1L);

        when(menuRepository.findByVisibleTrueOrderBySortOrder()).thenReturn(List.of(root, child, child2));

        var tree = menuService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).hasSize(2);
        verify(menuRepository).findByVisibleTrueOrderBySortOrder();
    }

    @Test
    void shouldListAllMenus() {
        var menu = new SysMenu();
        menu.setId(1L);
        menu.setName("test");
        when(menuRepository.findAll()).thenReturn(List.of(menu));

        var result = menuService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(menuRepository).findAll();
    }

    @Test
    void shouldGetMenuById() {
        var menu = new SysMenu();
        menu.setId(1L);
        menu.setName("test");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        var result = menuService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(menuRepository).findById(1L);
    }

    @Test
    void shouldCreateMenu() {
        var menu = new SysMenu();
        menu.setName("test");
        when(menuRepository.save(menu)).thenReturn(menu);

        var result = menuService.create(menu);

        assertThat(result).isEqualTo(menu);
        verify(menuRepository).save(menu);
    }

    @Test
    void shouldUpdateMenu() {
        var existing = new SysMenu();
        existing.setId(1L);
        existing.setName("old");

        var body = new SysMenu();
        body.setName("new");
        body.setPath("/new");
        body.setComponent("comp");
        body.setIcon("icon");
        body.setMenuType("M");
        body.setPerms("perms");
        body.setVisible(true);
        body.setSortOrder(5);
        body.setParentId(0L);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(menuRepository.save(existing)).thenReturn(existing);

        var result = menuService.update(1L, body);

        assertThat(result.getName()).isEqualTo("new");
        assertThat(result.getPath()).isEqualTo("/new");
        assertThat(result.getComponent()).isEqualTo("comp");
        assertThat(result.getIcon()).isEqualTo("icon");
        assertThat(result.getMenuType()).isEqualTo("M");
        assertThat(result.getPerms()).isEqualTo("perms");
        assertThat(result.getSortOrder()).isEqualTo(5);
        assertThat(result.getParentId()).isEqualTo(0L);
        verify(menuRepository).save(existing);
    }

    @Test
    void shouldThrowWhenUpdateNonExistentMenu() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        var body = new SysMenu();
        assertThatThrownBy(() -> menuService.update(99L, body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Menu not found");
    }

    @Test
    void shouldDeleteMenu() {
        menuService.delete(1L);

        verify(menuRepository).deleteById(1L);
    }

    @Test
    void shouldMoveMenuUp() {
        var menuA = new SysMenu();
        menuA.setId(1L);
        menuA.setSortOrder(10);
        menuA.setParentId(0L);

        var menuB = new SysMenu();
        menuB.setId(2L);
        menuB.setSortOrder(20);
        menuB.setParentId(0L);

        when(menuRepository.findById(2L)).thenReturn(Optional.of(menuB));
        when(menuRepository.findByParentId(0L)).thenReturn(new ArrayList<>(List.of(menuA, menuB)));

        menuService.move(2L, -1);

        assertThat(menuB.getSortOrder()).isEqualTo(10);
        assertThat(menuA.getSortOrder()).isEqualTo(20);
        verify(menuRepository, times(2)).save(any(SysMenu.class));
        verify(menuRepository).findById(2L);
        verify(menuRepository).findByParentId(0L);
    }

    @Test
    void shouldThrowWhenMoveNonExistentMenu() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.move(99L, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Menu not found");
    }
}

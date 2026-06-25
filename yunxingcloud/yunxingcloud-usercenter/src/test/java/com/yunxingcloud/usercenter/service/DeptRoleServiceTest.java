package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.usercenter.entity.Department;
import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.DepartmentRepository;
import com.yunxingcloud.usercenter.repository.RoleRepository;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeptRoleServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;

    private DeptRoleService service;

    @BeforeEach
    void setUp() {
        service = new DeptRoleService(departmentRepository, roleRepository, userRepository);
    }

    @Test void shouldBuildDepartmentTree() {
        Department root = new Department();
        root.setId(1L);
        root.setName("Root");
        root.setSortOrder(0);

        Department child = new Department();
        child.setId(2L);
        child.setName("Child");
        child.setParentId(1L);
        child.setSortOrder(1);

        when(departmentRepository.findByEnabledTrueOrderBySortOrder())
                .thenReturn(List.of(root, child));

        List<Department> tree = service.getDepartmentTree();
        assertEquals(1, tree.size());
        assertEquals("Root", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("Child", tree.get(0).getChildren().get(0).getName());
    }

    @Test void shouldGetUserPermissions() {
        Role role = new Role();
        role.setEnabled(true);
        role.setPermissions("user:read, user:write");

        User user = new User();
        user.setUsername("test");
        user.setRoles(Set.of(role));

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        List<String> perms = service.getUserPermissions("test");
        assertEquals(2, perms.size());
        assertTrue(perms.contains("user:read"));
        assertTrue(perms.contains("user:write"));
    }

    @Test void shouldReturnEmptyForUnknownUser() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertTrue(service.getUserPermissions("nobody").isEmpty());
        assertTrue(service.getUserRoles("nobody").isEmpty());
    }

    @Test void shouldSetUserDepartment() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        service.setUserDepartment(1L, 5L);
        assertEquals(5L, user.getDepartmentId());
        verify(userRepository).save(user);
    }
}

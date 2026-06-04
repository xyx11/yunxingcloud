package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.usercenter.entity.Department;
import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.DepartmentRepository;
import com.yunxingcloud.usercenter.repository.RoleRepository;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DeptRoleService {

    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DeptRoleService(DepartmentRepository departmentRepository,
                            RoleRepository roleRepository,
                            UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public List<Department> getDepartmentTree() {
        List<Department> all = departmentRepository.findByEnabledTrueOrderBySortOrder();
        Map<Long, Department> map = new HashMap<>();
        List<Department> roots = new ArrayList<>();

        for (Department dept : all) {
            map.put(dept.getId(), dept);
        }
        for (Department dept : all) {
            if (dept.getParentId() == null) {
                roots.add(dept);
            } else {
                Department parent = map.get(dept.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dept);
                }
            }
        }
        roots.sort(Comparator.comparingInt(d -> d.getSortOrder() != null ? d.getSortOrder() : 0));
        return roots;
    }

    public List<User> getDepartmentUsers(Long deptId) {
        return userRepository.findAll().stream()
                .filter(u -> deptId.equals(u.getDepartmentId()))
                .toList();
    }

    @Transactional
    public void setUserDepartment(Long userId, Long deptId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setDepartmentId(deptId);
            userRepository.save(user);
        });
    }

    @Transactional
    public void setUserRoles(Long userId, List<Long> roleIds) {
        userRepository.findById(userId).ifPresent(user -> {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
            userRepository.save(user);
        });
    }

    public List<String> getUserPermissions(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();

        Set<String> permissions = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            if (role.isEnabled() && role.getPermissions() != null && !role.getPermissions().isBlank()) {
                for (String perm : role.getPermissions().split(",")) {
                    permissions.add(perm.trim());
                }
            }
        }
        return new ArrayList<>(permissions);
    }

    public List<Role> getUserRoles(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();
        return new ArrayList<>(user.getRoles());
    }
}

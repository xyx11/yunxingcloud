package com.yunxingcloud.usercenter.config;

import com.yunxingcloud.usercenter.entity.Department;
import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.DepartmentRepository;
import com.yunxingcloud.usercenter.repository.RoleRepository;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                            DepartmentRepository departmentRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = null;
        if (!roleRepository.existsByCode("admin")) {
            adminRole = new Role("超级管理员", "admin", "系统超级管理员",
                    "user:read,user:write,dept:read,dept:write,role:read,role:write,menu:read,menu:write,config:read,config:write,job:read,job:write,operlog:read,operlog:write");
            adminRole = roleRepository.save(adminRole);
        } else {
            adminRole = roleRepository.findByCode("admin").orElse(null);
        }
        if (!roleRepository.existsByCode("user")) {
            roleRepository.save(new Role("普通用户", "user", "普通用户",
                    "user:read,dept:read"));
        }

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@yunxingcloud.com");
            admin.setNickname("管理员");
            admin.setRegisterSource("local");
            admin.setEnabled(true);
            if (adminRole != null) {
                admin.setRoles(Set.of(adminRole));
            }
            userRepository.save(admin);
        }

        if (departmentRepository.count() == 0) {
            Department root = new Department("总公司", null);
            root.setSortOrder(1);
            departmentRepository.save(root);
        }
    }
}

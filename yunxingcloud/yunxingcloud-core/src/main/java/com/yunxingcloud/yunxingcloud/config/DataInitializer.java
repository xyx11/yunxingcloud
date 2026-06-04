package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SysMenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                            SysMenuRepository menuRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@yunxingcloud.com");
            userRepository.save(admin);
        }

        if (menuRepository.count() == 0) {
            SysMenu system = createMenu("系统管理", null, 1, "system", null, "M", null);
            menuRepository.save(system);

            SysMenu userMgr = createMenu("用户管理", system.getId(), 1, "/users", "UserManageView", "C", "system:user:list");
            menuRepository.save(userMgr);
            SysMenu deptMgr = createMenu("部门管理", system.getId(), 2, "/departments", "DepartmentView", "C", "system:dept:list");
            menuRepository.save(deptMgr);
            SysMenu roleMgr = createMenu("角色管理", system.getId(), 3, "/roles", "RoleView", "C", "system:role:list");
            menuRepository.save(roleMgr);
            SysMenu menuMgr = createMenu("菜单管理", system.getId(), 4, "/menus", "MenuView", "C", "system:menu:list");
            menuRepository.save(menuMgr);

            SysMenu monitor = createMenu("系统监控", null, 2, "monitor", null, "M", null);
            menuRepository.save(monitor);
            SysMenu operlog = createMenu("操作日志", monitor.getId(), 1, "/operlog", "OperLogView", "C", "monitor:operlog:list");
            menuRepository.save(operlog);
            SysMenu job = createMenu("定时任务", monitor.getId(), 2, "/job", "JobView", "C", "monitor:job:list");
            menuRepository.save(job);
            SysMenu apiDoc = createMenu("API文档", monitor.getId(), 3, "/swagger", "SwaggerView", "C", "monitor:swagger:view");
            menuRepository.save(apiDoc);

            SysMenu tools = createMenu("系统工具", null, 3, "tools", null, "M", null);
            menuRepository.save(tools);
            SysMenu gen = createMenu("代码生成", tools.getId(), 1, "/generator", "GenView", "C", "tool:gen:list");
            menuRepository.save(gen);
            SysMenu config = createMenu("参数配置", tools.getId(), 2, "/config", "ConfigView", "C", "tool:config:list");
            menuRepository.save(config);
        }
    }

    private SysMenu createMenu(String name, Long parentId, int sort, String path, String component, String type, String perms) {
        SysMenu m = new SysMenu();
        m.setName(name);
        m.setParentId(parentId);
        m.setSortOrder(sort);
        m.setPath(path);
        m.setComponent(component);
        m.setMenuType(type);
        m.setPerms(perms);
        m.setVisible(true);
        return m;
    }
}

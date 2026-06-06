package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SysMenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                            SysMenuRepository menuRepository,
                            PasswordEncoder passwordEncoder,
                            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initDepts();
        initUsers();
        initMenus();
        initDemoData();
    }

    private void initRoles() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS role (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    code VARCHAR(50) NOT NULL UNIQUE,
                    description VARCHAR(200),
                    permissions VARCHAR(2000) DEFAULT '',
                    enabled BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_roles (
                    user_id BIGINT NOT NULL,
                    role_id BIGINT NOT NULL,
                    PRIMARY KEY (user_id, role_id)
                )""");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS department (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    parent_id BIGINT,
                    sort_order INT DEFAULT 0,
                    enabled BOOLEAN DEFAULT TRUE
                )""");
        } catch (Exception ignored) {}

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM role", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions) VALUES (?,?,?,?)",
                "超级管理员", "admin", "系统超级管理员",
                "user:read,user:write,dept:read,dept:write,role:read,role:write,menu:read,menu:write,config:read,config:write,job:read,job:write,operlog:read,operlog:write");
            jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions) VALUES (?,?,?,?)",
                "普通用户", "user", "普通用户", "user:read,dept:read");
        }
    }

    private void initDepts() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM department", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO department (name, parent_id, sort_order) VALUES (?,?,?)", "总公司", null, 1);
            jdbcTemplate.update("INSERT INTO department (name, parent_id, sort_order) VALUES (?,?,?)", "研发部", 1, 1);
            jdbcTemplate.update("INSERT INTO department (name, parent_id, sort_order) VALUES (?,?,?)", "市场部", 1, 2);
        }
    }

    private void initUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@yunxingcloud.com");
            admin = userRepository.save(admin);
            // assign admin role
            try {
                Long roleId = jdbcTemplate.queryForObject(
                    "SELECT id FROM role WHERE code = ?", Long.class, "admin");
                if (roleId != null) {
                    jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id) VALUES (?,?)",
                        admin.getId(), roleId);
                }
            } catch (Exception ignored) {}
        }
    }

    private void initMenus() {

        if (menuRepository.count() == 0) {
            SysMenu system = createMenu("系统管理", null, 1, "", null, "M", null);
            menuRepository.save(system);

            SysMenu userMgr = createMenu("用户管理", system.getId(), 1, "/users", "UserManageView", "C", "system:user:list");
            menuRepository.save(userMgr);
            SysMenu deptMgr = createMenu("部门管理", system.getId(), 2, "/departments", "DepartmentView", "C", "system:dept:list");
            menuRepository.save(deptMgr);
            SysMenu roleMgr = createMenu("角色管理", system.getId(), 3, "/roles", "RoleView", "C", "system:role:list");
            menuRepository.save(roleMgr);
            SysMenu menuMgr = createMenu("菜单管理", system.getId(), 4, "/menus", "MenuView", "C", "system:menu:list");
            menuRepository.save(menuMgr);

            SysMenu monitor = createMenu("系统监控", null, 2, "", null, "M", null);
            menuRepository.save(monitor);
            SysMenu operlog = createMenu("操作日志", monitor.getId(), 1, "/operlog", "OperLogView", "C", "monitor:operlog:list");
            menuRepository.save(operlog);
            SysMenu job = createMenu("定时任务", monitor.getId(), 2, "/job", "JobView", "C", "monitor:job:list");
            menuRepository.save(job);
            SysMenu apiDoc = createMenu("API文档", monitor.getId(), 3, "/swagger", "SwaggerView", "C", "monitor:swagger:view");
            SysMenu sysMonitor = createMenu("系统监控", monitor.getId(), 4, "/monitor", "SystemMonitorView", "C", "monitor:system:view");
            menuRepository.save(sysMonitor);
            menuRepository.save(apiDoc);

            SysMenu tools = createMenu("系统工具", null, 3, "", null, "M", null);
            menuRepository.save(tools);
            SysMenu gen = createMenu("代码生成", tools.getId(), 1, "/generator", "GenView", "C", "tool:gen:list");
            menuRepository.save(gen);
            SysMenu config = createMenu("参数配置", tools.getId(), 2, "/config", "ConfigView", "C", "tool:config:list");
            menuRepository.save(config);
        }
    }

    private void initDemoData() {
        if (!userRepository.existsByUsername("demo")) {
            User demo = new User("demo", passwordEncoder.encode("demo1234"), "demo@yunxingcloud.com");
            demo.setNickname("演示用户");
            demo.setRegisterSource("local");
            userRepository.save(demo);
            try {
                Long roleId = jdbcTemplate.queryForObject(
                    "SELECT id FROM role WHERE code = ?", Long.class, "user");
                if (roleId != null) {
                    jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id) VALUES (?,?)", demo.getId(), roleId);
                }
            } catch (Exception ignored) {}
        }

        // 演示系统配置
        try {
            Integer cfgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_config", Integer.class);
            if (cfgCount != null && cfgCount == 0) {
                jdbcTemplate.update("INSERT INTO sys_config (name, config_key, config_value, config_type) VALUES (?,?,?,?)",
                        "系统名称", "sys.name", "yunxingcloud", "Y");
                jdbcTemplate.update("INSERT INTO sys_config (name, config_key, config_value, config_type) VALUES (?,?,?,?)",
                        "系统版本", "sys.version", "1.0.0", "Y");
                jdbcTemplate.update("INSERT INTO sys_config (name, config_key, config_value, config_type) VALUES (?,?,?,?)",
                        "每页条数", "sys.pageSize", "10", "Y");
            }
        } catch (Exception ignored) {}
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

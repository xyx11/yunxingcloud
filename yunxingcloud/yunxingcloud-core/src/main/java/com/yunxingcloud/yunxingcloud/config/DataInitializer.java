package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

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
        initNewModuleMenus();
        initDemoData();
        initMissingMenus();
    }

    private void initRoles() {
        // Flyway V2/V6 creates these tables in prod; here for test profile (flyway.enabled=false)
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS role (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, code VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(200), permissions VARCHAR(2000) DEFAULT '', enabled BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user_roles (user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, PRIMARY KEY (user_id, role_id))");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS department (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, parent_id BIGINT, sort_order INT DEFAULT 0, enabled BOOLEAN DEFAULT TRUE)");
        } catch (Exception ignored) {}

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM role", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions) VALUES (?,?,?,?)",
                "超级管理员", "admin", "系统超级管理员",
                "admin,user:read,user:write,dept:read,dept:write,role:read,role:write,menu:read,menu:write,config:read,config:write,job:read,job:write,operlog:read,operlog:write,file:write,dict:read,dict:write,notice:read,notice:write,post:read,post:write,logininfor:read,logininfor:write,ticket:read,ticket:write,monitor:banner:list,monitor:review:list,monitor:import:list,monitor:sku:list,monitor:shipment:list");
            jdbcTemplate.update(
                "INSERT INTO role (name, code, description, permissions) VALUES (?,?,?,?)",
                "普通用户", "user", "普通用户", "user:read,dept:read,ticket:read,ticket:write,monitor:banner:list,monitor:review:list,monitor:import:list,monitor:sku:list,monitor:shipment:list");
        }

        // 确保 admin 角色始终拥有最新权限（增量更新）
        try {
            String newPerms = "admin,user:read,user:write,dept:read,dept:write,role:read,role:write,menu:read,menu:write,config:read,config:write,job:read,job:write,operlog:read,operlog:write,file:write,dict:read,dict:write,notice:read,notice:write,post:read,post:write,logininfor:read,logininfor:write,ticket:read,ticket:write,monitor:banner:list,monitor:review:list,monitor:import:list,monitor:sku:list,monitor:shipment:list";
            jdbcTemplate.update("UPDATE role SET permissions = ? WHERE code = 'admin'", newPerms);
        } catch (Exception ignored) {}
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
            SysMenu postMgr = createMenu("岗位管理", system.getId(), 5, "/posts", "PostView", "C", "system:post:list");
            menuRepository.save(postMgr);

            SysMenu monitor = createMenu("系统监控", null, 2, "", null, "M", null);
            menuRepository.save(monitor);
            SysMenu operlog = createMenu("操作日志", monitor.getId(), 1, "/operlog", "OperLogView", "C", "monitor:operlog:list");
            menuRepository.save(operlog);
            SysMenu job = createMenu("定时任务", monitor.getId(), 2, "/job", "JobView", "C", "monitor:job:list");
            menuRepository.save(job);
            SysMenu apiDoc = createMenu("API文档", monitor.getId(), 3, "/swagger", "SwaggerView", "C", "monitor:swagger:view");
            SysMenu sysMonitor = createMenu("系统监控", monitor.getId(), 4, "/monitor", "SystemMonitorView", "C", "monitor:system:view");
            menuRepository.save(apiDoc);
            menuRepository.save(sysMonitor);
            SysMenu loginlog = createMenu("登录日志", monitor.getId(), 5, "/loginlog", "LoginLogView", "C", "monitor:logininfor:list");
            menuRepository.save(loginlog);
            SysMenu online = createMenu("在线用户", monitor.getId(), 6, "/online", "OnlineUserView", "C", "monitor:online:list");
            menuRepository.save(online);

            SysMenu tools = createMenu("系统工具", null, 3, "", null, "M", null);
            menuRepository.save(tools);
            SysMenu gen = createMenu("代码生成", tools.getId(), 1, "/generator", "GenView", "C", "tool:gen:list");
            menuRepository.save(gen);
            SysMenu config = createMenu("参数配置", tools.getId(), 2, "/config", "ConfigView", "C", "tool:config:list");
            menuRepository.save(config);
            SysMenu maintenance = createMenu("数据维护", tools.getId(), 3, "/maintenance", "MaintenanceView", "C", "tool:maintenance:view");
            menuRepository.save(maintenance);
            SysMenu dictMgr = createMenu("字典管理", tools.getId(), 4, "/dict", "DictView", "C", "system:dict:list");
            menuRepository.save(dictMgr);
            SysMenu noticeMgr = createMenu("通知公告", tools.getId(), 5, "/notices", "NoticeView", "C", "system:notice:list");
            menuRepository.save(noticeMgr);
            SysMenu backupMgr = createMenu("数据备份", tools.getId(), 6, "/backup", "BackupView", "C", "tool:backup:list");
            menuRepository.save(backupMgr);
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

        // 种子字典数据
        try {
            Integer dtCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict_type", Integer.class);
            if (dtCount != null && dtCount == 0) {
                jdbcTemplate.update("INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES (?,?,?)",
                        "用户状态", "sys_user_status", "0");
                jdbcTemplate.update("INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES (?,?,?)",
                        "用户来源", "sys_user_source", "0");
                jdbcTemplate.update("INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES (?,?,?)",
                        "通知类型", "sys_notice_type", "0");
                jdbcTemplate.update("INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES (?,?,?)",
                        "通用状态", "sys_common_status", "0");

                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_status", "正常", "0", "Y", 1);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_status", "停用", "1", "N", 2);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_source", "本地注册", "local", "Y", 1);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_source", "微信", "wechat", "N", 2);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_source", "QQ", "qq", "N", 3);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_user_source", "支付宝", "alipay", "N", 4);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_notice_type", "通知", "1", "Y", 1);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_notice_type", "公告", "2", "N", 2);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_common_status", "正常", "0", "Y", 1);
                jdbcTemplate.update("INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES (?,?,?,?,?)",
                        "sys_common_status", "停用", "1", "N", 2);
            }
        } catch (Exception ignored) {}

        // 种子岗位数据
        try {
            Integer postCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_post", Integer.class);
            if (postCount != null && postCount == 0) {
                jdbcTemplate.update("INSERT INTO sys_post (post_code, post_name, sort_order) VALUES (?,?,?)",
                        "ceo", "首席执行官", 1);
                jdbcTemplate.update("INSERT INTO sys_post (post_code, post_name, sort_order) VALUES (?,?,?)",
                        "cto", "首席技术官", 2);
                jdbcTemplate.update("INSERT INTO sys_post (post_code, post_name, sort_order) VALUES (?,?,?)",
                        "dev", "开发工程师", 3);
                jdbcTemplate.update("INSERT INTO sys_post (post_code, post_name, sort_order) VALUES (?,?,?)",
                        "pm", "产品经理", 4);
                jdbcTemplate.update("INSERT INTO sys_post (post_code, post_name, sort_order) VALUES (?,?,?)",
                        "hr", "人力资源", 5);
            }
        } catch (Exception ignored) {}

        // 种子通知公告
        try {
            Integer noticeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_notice", Integer.class);
            if (noticeCount != null && noticeCount == 0) {
                jdbcTemplate.update("INSERT INTO sys_notice (notice_title, notice_type, notice_content, status) VALUES (?,?,?,?)",
                        "系统上线通知", "1", "yunxingcloud 系统已正式上线运行，欢迎使用！如有问题请联系管理员。", "0");
                jdbcTemplate.update("INSERT INTO sys_notice (notice_title, notice_type, notice_content, status) VALUES (?,?,?,?)",
                        "版本更新公告", "2", "系统已升级至 v1.0.0 版本，新增字典管理、通知公告、岗位管理、登录日志等功能模块。", "0");
                jdbcTemplate.update("INSERT INTO sys_notice (notice_title, notice_type, notice_content, status) VALUES (?,?,?,?)",
                        "安全提醒", "1", "请定期修改密码，不要将账号密码泄露给他人，确保系统安全。", "0");
            }
        } catch (Exception ignored) {}

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

    private void initNewModuleMenus() {
        // 确保新模块菜单存在（不依赖 count()==0 条件）
        try {
            List<SysMenu> all = menuRepository.findAll();
            Set<String> paths = new HashSet<>();
            Map<String, Long> pathToId = new HashMap<>();
            for (SysMenu m : all) {
                if (m.getPath() != null && !m.getPath().isEmpty()) paths.add(m.getPath());
                if (m.getComponent() != null) pathToId.put(m.getComponent(), m.getId());
            }

            // 查找"系统管理"父菜单
            SysMenu systemParent = all.stream().filter(m -> "系统管理".equals(m.getName()) && m.getParentId() == null).findFirst().orElse(null);
            // 查找"系统监控"父菜单
            SysMenu monitorParent = all.stream().filter(m -> "系统监控".equals(m.getName()) && m.getParentId() == null).findFirst().orElse(null);
            // 查找"系统工具"父菜单
            SysMenu toolsParent = all.stream().filter(m -> "系统工具".equals(m.getName()) && m.getParentId() == null).findFirst().orElse(null);

            if (systemParent != null && !paths.contains("/posts")) {
                menuRepository.save(createMenu("岗位管理", systemParent.getId(), 5, "/posts", "PostView", "C", "system:post:list"));
            }
            if (systemParent != null && !paths.contains("/tickets")) {
                menuRepository.save(createMenu("工单管理", systemParent.getId(), 6, "/tickets", "TicketView", "C", "ticket:read"));
            }
            if (systemParent != null && !paths.contains("/payments")) {
                menuRepository.save(createMenu("支付管理", systemParent.getId(), 7, "/payments", "PaymentView", "C", "ticket:read"));
            }
            if (systemParent != null && !paths.contains("/orders")) {
                menuRepository.save(createMenu("订单管理", systemParent.getId(), 8, "/orders", "OrderView", "C", "ticket:read"));
            }
            if (systemParent != null && !paths.contains("/products")) {
                menuRepository.save(createMenu("商品管理", systemParent.getId(), 9, "/products", "ProductView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/inventory")) {
                menuRepository.save(createMenu("库存管理", systemParent.getId(), 10, "/inventory", "InventoryView", "C", "ticket:read"));
            }
            if (systemParent != null && !paths.contains("/warehouses")) {
                menuRepository.save(createMenu("仓库管理", systemParent.getId(), 11, "/warehouses", "WarehouseView", "C", "ticket:write"));
            }
            // v2.2+ 营销工具
            if (systemParent != null && !paths.contains("/groupbuy")) {
                menuRepository.save(createMenu("拼团管理", systemParent.getId(), 12, "/groupbuy", "GroupBuyView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/flashsale")) {
                menuRepository.save(createMenu("秒杀管理", systemParent.getId(), 13, "/flashsale", "FlashSaleView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/aftersale")) {
                menuRepository.save(createMenu("售后管理", systemParent.getId(), 14, "/aftersale", "AfterSaleView", "C", "ticket:write"));
            }
            // v2.3+ 内容运营
            if (systemParent != null && !paths.contains("/articles")) {
                menuRepository.save(createMenu("内容管理", systemParent.getId(), 15, "/articles", "ArticleView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/notifications")) {
                menuRepository.save(createMenu("通知管理", systemParent.getId(), 16, "/notifications", "NotificationView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/giftcards")) {
                menuRepository.save(createMenu("礼品卡管理", systemParent.getId(), 17, "/giftcards", "GiftCardView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/campaigns")) {
                menuRepository.save(createMenu("营销活动", systemParent.getId(), 18, "/campaigns", "CampaignView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/tags")) {
                menuRepository.save(createMenu("标签管理", systemParent.getId(), 19, "/tags", "TagView", "C", "ticket:write"));
            }
            if (systemParent != null && !paths.contains("/member-tiers")) {
                menuRepository.save(createMenu("会员等级", systemParent.getId(), 20, "/member-tiers", "MemberTierView", "C", "ticket:write"));
            }
            if (monitorParent != null) {
                if (!paths.contains("/loginlog")) {
                    menuRepository.save(createMenu("登录日志", monitorParent.getId(), 5, "/loginlog", "LoginLogView", "C", "monitor:logininfor:list"));
                }
                if (!paths.contains("/online")) {
                    menuRepository.save(createMenu("在线用户", monitorParent.getId(), 6, "/online", "OnlineUserView", "C", "monitor:online:list"));
                }
            }
            if (toolsParent != null) {
                if (!paths.contains("/dict")) {
                    menuRepository.save(createMenu("字典管理", toolsParent.getId(), 4, "/dict", "DictView", "C", "system:dict:list"));
                }
                if (!paths.contains("/notices")) {
                    menuRepository.save(createMenu("通知公告", toolsParent.getId(), 5, "/notices", "NoticeView", "C", "system:notice:list"));
                }
                if (!paths.contains("/backup")) {
                    menuRepository.save(createMenu("数据备份", toolsParent.getId(), 6, "/backup", "BackupView", "C", "tool:backup:list"));
                }
                if (!paths.contains("/messages")) {
                    menuRepository.save(createMenu("站内信", toolsParent.getId(), 7, "/messages", "MessageView", "C", "tool:message:list"));
                }
            }
        } catch (Exception ignored) {}
    }

    /** 增量添加新页面菜单项 */
    private void initMissingMenus() {
        try {
            Long monitorId = jdbcTemplate.queryForObject("SELECT id FROM sys_menu WHERE path='' AND parent_id IS NULL AND name='系统监控'", Long.class);
            if (monitorId == null) return;
            if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE path='banners'", Integer.class) == 0) {
                jdbcTemplate.update("INSERT INTO sys_menu (name,parent_id,sort_order,path,component,menu_type,perms,visible,created_at) VALUES (?,?,?,?,?,?,?,?,?)",
                    "Banner管理", monitorId, 7, "banners", "BannerView", "C", "monitor:banner:list", 1, java.time.LocalDateTime.now());
            if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE path='products/import'", Integer.class) == 0) { jdbcTemplate.update("INSERT INTO sys_menu (name,parent_id,sort_order,path,component,menu_type,perms,visible,created_at) VALUES (?,?,?,?,?,?,?,?,?)", "批量导入", monitorId, 9, "products/import", "ProductImportView", "C", "monitor:import:list", 1, java.time.LocalDateTime.now()); }
            }
            if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE path='reviews'", Integer.class) == 0) {
                jdbcTemplate.update("INSERT INTO sys_menu (name,parent_id,sort_order,path,component,menu_type,perms,visible,created_at) VALUES (?,?,?,?,?,?,?,?,?)",
                    "评价管理", monitorId, 8, "reviews", "ReviewManageView", "C", "monitor:review:list", 1, java.time.LocalDateTime.now());
            }
        } catch (Exception e) { log.warn("initMissingMenus: {}", e.getMessage()); }
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

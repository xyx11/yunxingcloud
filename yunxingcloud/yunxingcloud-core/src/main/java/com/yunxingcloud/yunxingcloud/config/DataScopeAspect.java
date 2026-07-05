package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.common.annotation.DataScope;
import com.yunxingcloud.common.core.BaseQueryParams;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataScopeAspect {

    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);
    private final JdbcTemplate jdbc;

    public DataScopeAspect(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Before("@annotation(ds)")
    public void applyDataScope(JoinPoint point, DataScope ds) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || "anonymousUser".equals(username)) return;
        try {
            String sql = buildScope(username, ds);
            if (sql != null) {
                for (Object arg : point.getArgs()) {
                    if (arg instanceof BaseQueryParams p) { p.setDataScope(sql); break; }
                }
            }
        } catch (Exception e) { log.warn("数据权限解析失败: {}", e.getMessage()); }
    }

    private String buildScope(String username, DataScope ds) {
        try {
            Long c = jdbc.queryForObject("SELECT COUNT(*) FROM user_roles ur JOIN role r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE u.username=? AND r.code='admin'", Long.class, username);
            if (c != null && c > 0) return null;
        } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
        Long deptId = null;
        try { deptId = jdbc.queryForObject("SELECT department_id FROM users WHERE username=?", Long.class, username); } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
        if (deptId != null) return String.format(" %s IN (SELECT id FROM department WHERE id=%d OR parent_id=%d) ", ds.deptAlias(), deptId, deptId);
        return String.format(" %s='%s' ", ds.userAlias(), username);
    }
}

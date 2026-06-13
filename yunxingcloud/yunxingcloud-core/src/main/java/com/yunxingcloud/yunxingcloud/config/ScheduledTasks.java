package com.yunxingcloud.yunxingcloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final JdbcTemplate jdbc;

    public ScheduledTasks(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点
    public void cleanOldLogs() {
        try {
            int ops = jdbc.update("DELETE FROM sys_oper_log WHERE oper_time < ?", LocalDateTime.now().minusDays(90));
            int logins = jdbc.update("DELETE FROM sys_logininfor WHERE login_time < ?", LocalDateTime.now().minusDays(180));
            log.info("自动清理: 操作日志{}条, 登录日志{}条", ops, logins);
        } catch (Exception e) { log.warn("自动清理失败: {}", e.getMessage()); }
    }
}

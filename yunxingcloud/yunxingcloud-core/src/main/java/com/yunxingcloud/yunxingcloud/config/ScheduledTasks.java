package com.yunxingcloud.yunxingcloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final JdbcTemplate jdbc;

    @Value("${app.cleanup.oper-log-days:90}")
    private int operLogRetentionDays;

    @Value("${app.cleanup.login-log-days:180}")
    private int loginLogRetentionDays;

    public ScheduledTasks(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldLogs() {
        try {
            int ops = jdbc.update("DELETE FROM sys_oper_log WHERE oper_time < ?", LocalDateTime.now().minusDays(operLogRetentionDays));
            int logins = jdbc.update("DELETE FROM sys_logininfor WHERE login_time < ?", LocalDateTime.now().minusDays(loginLogRetentionDays));
            log.info("Auto cleanup: {} oper logs, {} login logs", ops, logins);
        } catch (Exception e) { log.warn("Auto cleanup failed: {}", e.getMessage()); }
    }
}

package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.yunxingcloud.repository.SysMenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);
    private final SysMenuRepository menuRepository;
    private final JdbcTemplate jdbcTemplate;

    public CacheWarmer(SysMenuRepository menuRepository, JdbcTemplate jdbcTemplate) {
        this.menuRepository = menuRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        log.info("缓存预热开始...");
        long start = System.currentTimeMillis();
        try {
            menuRepository.findByVisibleTrueOrderBySortOrder();
            jdbcTemplate.queryForList("SELECT config_key, config_value FROM sys_config");
            jdbcTemplate.queryForList("SELECT id, name, code FROM role");
            jdbcTemplate.queryForList("SELECT id, name FROM department");
        } catch (Exception e) {
            log.warn("缓存预热部分失败: {}", e.getMessage());
        }
        log.info("缓存预热完成 ({}ms)", System.currentTimeMillis() - start);
    }
}

package com.yunxingcloud.yunxingcloud.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    public FlywayConfig(DataSource dataSource,
                        @Value("${spring.flyway.locations:classpath:db/migration}") String locations) {
        log.info("开始执行 Flyway 迁移...");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations)
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)
                    .load();
            flyway.migrate();
            log.info("Flyway 迁移完成");
        } catch (Exception e) {
            log.warn("Flyway 迁移失败: {}. 应用继续启动。", e.getMessage());
        }
    }
}

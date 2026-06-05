package com.yunxingcloud.yunxingcloud.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeatureFlags {

    private final JdbcTemplate jdbcTemplate;

    public FeatureFlags(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable("featureFlags")
    public Map<String, String> getAll() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT config_key, config_value FROM sys_config WHERE config_key LIKE 'feature.%' OR config_key = 'sys.announcement'");
            return rows.stream().collect(Collectors.toMap(
                r -> (String) r.get("config_key"),
                r -> (String) r.get("config_value"),
                (a, b) -> b, LinkedHashMap::new));
        } catch (Exception e) {
            return Map.of();
        }
    }

    public boolean isEnabled(String feature) {
        String val = getAll().getOrDefault("feature." + feature, "false");
        return "true".equalsIgnoreCase(val);
    }

    public String getAnnouncement() {
        return getAll().getOrDefault("sys.announcement", "");
    }
}

package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import com.yunxingcloud.yunxingcloud.repository.SysConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.yunxingcloud.common.core.CsvUtils;

@Service
public class ConfigService {

    private final SysConfigRepository configRepository;
    private final FeatureFlags featureFlags;

    public ConfigService(SysConfigRepository configRepository, FeatureFlags featureFlags) {
        this.configRepository = configRepository;
        this.featureFlags = featureFlags;
    }

    private void refreshIfFeature(SysConfig c) {
        if (c.getConfigKey() != null && c.getConfigKey().startsWith("feature.")) {
            featureFlags.refresh();
        }
    }

    public List<SysConfig> list() {
        return configRepository.findAll();
    }

    public Optional<SysConfig> getByKey(String key) {
        return configRepository.findByConfigKey(key);
    }

    @Transactional
    public SysConfig create(SysConfig config) {
        if (configRepository.existsByConfigKey(config.getConfigKey())) {
            throw new IllegalArgumentException("config.key_exists");
        }
        SysConfig saved = configRepository.save(config);
        refreshIfFeature(saved);
        return saved;
    }

    @Transactional
    public SysConfig update(Long id, SysConfig body) {
        return configRepository.findById(id).map(c -> {
            c.setName(body.getName());
            c.setConfigKey(body.getConfigKey());
            c.setConfigValue(body.getConfigValue());
            c.setConfigType(body.getConfigType());
            SysConfig updated = configRepository.save(c);
            refreshIfFeature(updated);
            return updated;
        }).orElseThrow(() -> new IllegalArgumentException("Config not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        SysConfig c = configRepository.findById(id).orElse(null);
        configRepository.deleteById(id);
        if (c != null) refreshIfFeature(c);
    }

    public String exportCsv() {
        List<String[]> rows = configRepository.findAll().stream()
                .map(c -> new String[]{c.getName(), c.getConfigKey(), c.getConfigValue(),
                        "Y".equals(c.getConfigType()) ? "是" : "否", c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""})
                .toList();
        return CsvUtils.toCsv(new String[]{"名称", "键名", "键值", "系统内置", "创建时间"}, rows);
    }
}

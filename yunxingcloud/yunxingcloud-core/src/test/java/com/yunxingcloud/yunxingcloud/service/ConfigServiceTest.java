package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import com.yunxingcloud.yunxingcloud.repository.SysConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock private SysConfigRepository configRepository;
    @Mock private FeatureFlags featureFlags;
    @InjectMocks private ConfigService configService;

    private SysConfig sampleConfig;
    private SysConfig featureConfig;
    private SysConfig updateBody;

    @BeforeEach
    void setUp() {
        sampleConfig = new SysConfig();
        sampleConfig.setId(1L);
        sampleConfig.setName("测试配置");
        sampleConfig.setConfigKey("test.key");
        sampleConfig.setConfigValue("test-value");
        sampleConfig.setConfigType("Y");

        featureConfig = new SysConfig();
        featureConfig.setId(2L);
        featureConfig.setName("功能开关");
        featureConfig.setConfigKey("feature.test");
        featureConfig.setConfigValue("true");
        featureConfig.setConfigType("Y");

        updateBody = new SysConfig();
        updateBody.setName("更新名称");
        updateBody.setConfigKey("updated.key");
        updateBody.setConfigValue("updated-value");
        updateBody.setConfigType("N");
    }

    @Test
    void shouldListAllConfigs() {
        when(configRepository.findAll()).thenReturn(List.of(sampleConfig, featureConfig));

        var result = configService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getConfigKey()).isEqualTo("test.key");
        assertThat(result.get(1).getConfigKey()).isEqualTo("feature.test");
    }

    @Test
    void shouldGetConfigByKey() {
        when(configRepository.findByConfigKey("test.key")).thenReturn(Optional.of(sampleConfig));

        var result = configService.getByKey("test.key");

        assertThat(result).isPresent();
        assertThat(result.get().getConfigValue()).isEqualTo("test-value");
    }

    @Test
    void shouldCreateConfig() {
        when(configRepository.existsByConfigKey("test.key")).thenReturn(false);
        when(configRepository.save(any())).thenReturn(sampleConfig);

        var result = configService.create(sampleConfig);

        assertThat(result).isNotNull();
        assertThat(result.getConfigKey()).isEqualTo("test.key");
        verify(configRepository).save(sampleConfig);
        verify(featureFlags, never()).refresh();
    }

    @Test
    void shouldThrowWhenCreateDuplicateConfigKey() {
        when(configRepository.existsByConfigKey("test.key")).thenReturn(true);

        assertThatThrownBy(() -> configService.create(sampleConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("config.key_exists");

        verify(configRepository, never()).save(any());
    }

    @Test
    void shouldRefreshFeatureWhenCreateFeatureConfig() {
        when(configRepository.existsByConfigKey("feature.test")).thenReturn(false);
        when(configRepository.save(any())).thenReturn(featureConfig);

        var result = configService.create(featureConfig);

        assertThat(result).isNotNull();
        assertThat(result.getConfigKey()).isEqualTo("feature.test");
        verify(configRepository).save(featureConfig);
        verify(featureFlags).refresh();
    }

    @Test
    void shouldUpdateConfig() {
        when(configRepository.findById(1L)).thenReturn(Optional.of(sampleConfig));
        when(configRepository.save(any())).thenReturn(sampleConfig);

        var result = configService.update(1L, updateBody);

        assertThat(result.getName()).isEqualTo("更新名称");
        assertThat(result.getConfigKey()).isEqualTo("updated.key");
        assertThat(result.getConfigValue()).isEqualTo("updated-value");
        assertThat(result.getConfigType()).isEqualTo("N");
        verify(configRepository).save(sampleConfig);
        verify(featureFlags, never()).refresh();
    }

    @Test
    void shouldThrowWhenUpdateNonExistentConfig() {
        when(configRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configService.update(99L, updateBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Config not found: 99");

        verify(configRepository, never()).save(any());
    }

    @Test
    void shouldDeleteConfig() {
        when(configRepository.findById(2L)).thenReturn(Optional.of(featureConfig));

        configService.delete(2L);

        verify(configRepository).deleteById(2L);
        verify(featureFlags).refresh();
    }

    @Test
    void shouldExportCsv() {
        when(configRepository.findAll()).thenReturn(List.of(sampleConfig));

        String csv = configService.exportCsv();

        assertThat(csv).startsWith("名称,键名,键值,系统内置,创建时间\n");
        assertThat(csv).contains("测试配置,test.key,test-value,是");
    }
}

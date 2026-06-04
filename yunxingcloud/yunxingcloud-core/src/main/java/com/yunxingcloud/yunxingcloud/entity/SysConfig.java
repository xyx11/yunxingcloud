package com.yunxingcloud.yunxingcloud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_config")
public class SysConfig {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(nullable = false, length = 500)
    private String configValue;

    @Column(length = 1)
    private String configType = "Y";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public SysConfig() {}

    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getConfigType() { return configType; }
    public void setConfigType(String configType) { this.configType = configType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

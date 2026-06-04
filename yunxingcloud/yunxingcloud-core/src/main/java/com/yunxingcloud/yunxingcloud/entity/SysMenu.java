package com.yunxingcloud.yunxingcloud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_menu")
public class SysMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 200)
    private String path;

    @Column(length = 200)
    private String component;

    @Column(length = 100)
    private String icon;

    @Column(length = 1)
    private String menuType = "M";

    @Column(length = 100)
    private String perms;

    private boolean visible = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private List<SysMenu> children = new ArrayList<>();

    public SysMenu() {}

    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SysMenu> getChildren() { return children; }
    public void setChildren(List<SysMenu> children) { this.children = children; }
}

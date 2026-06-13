package com.yunxingcloud.api.dto;

import java.util.List;

public class DepartmentDTO {
    private Long id;
    private String name;
    private Long parentId;
    private int sortOrder;
    private boolean enabled;
    private List<DepartmentDTO> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<DepartmentDTO> getChildren() { return children; }
    public void setChildren(List<DepartmentDTO> children) { this.children = children; }
}

package com.yunxingcloud.usercenter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    private boolean enabled = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private List<Department> children = new ArrayList<>();

    public Department() {}

    public Department(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Department> getChildren() { return children; }
    public void setChildren(List<Department> children) { this.children = children; }
}

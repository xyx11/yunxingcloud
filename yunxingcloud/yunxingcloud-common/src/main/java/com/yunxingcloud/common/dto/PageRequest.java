package com.yunxingcloud.common.dto;

import jakarta.validation.constraints.Min;

public class PageRequest {

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 20;

    private String sort;       // field,asc|desc
    private String keyword;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public org.springframework.data.domain.PageRequest toSpring() {
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            org.springframework.data.domain.Sort.Direction dir =
                parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.ASC;
            return org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(dir, parts[0]));
        }
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}

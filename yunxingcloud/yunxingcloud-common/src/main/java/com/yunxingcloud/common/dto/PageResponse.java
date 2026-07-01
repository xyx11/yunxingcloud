package com.yunxingcloud.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageResponse() {}

    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    public static <T> PageResponse<T> of(List<T> content, long total, int page, int size) {
        return new PageResponse<>(content, page, size, total,
            size > 0 ? (int) Math.ceil((double) total / size) : 0);
    }

    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0);
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}

package com.yunxingcloud.api.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentClientFallbackTest {

    private final DepartmentClientFallback fallback = new DepartmentClientFallback();

    @Test
    void treeShouldReturnEmptyList() {
        assertTrue(fallback.tree().isEmpty());
    }

    @Test
    void getByIdShouldReturnNull() {
        assertNull(fallback.getById(1L));
    }
}

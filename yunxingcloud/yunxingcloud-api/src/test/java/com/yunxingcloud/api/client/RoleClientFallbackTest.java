package com.yunxingcloud.api.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleClientFallbackTest {

    private final RoleClientFallback fallback = new RoleClientFallback();

    @Test
    void listShouldReturnEmptyList() {
        assertTrue(fallback.list().isEmpty());
    }

    @Test
    void getByIdShouldReturnNull() {
        assertNull(fallback.getById(1L));
    }
}

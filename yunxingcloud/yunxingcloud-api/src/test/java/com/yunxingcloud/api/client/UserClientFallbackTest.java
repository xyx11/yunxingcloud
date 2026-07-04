package com.yunxingcloud.api.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserClientFallbackTest {

    private final UserClientFallback fallback = new UserClientFallback();

    @Test
    void currentUserShouldReturnEmptyMap() {
        assertTrue(fallback.currentUser().isEmpty());
    }

    @Test
    void getByUsernameShouldReturnNull() {
        assertNull(fallback.getByUsername("test"));
    }

    @Test
    void permissionsShouldReturnEmptyList() {
        assertTrue(fallback.permissions().isEmpty());
    }
}

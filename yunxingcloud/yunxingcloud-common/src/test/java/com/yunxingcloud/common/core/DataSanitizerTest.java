package com.yunxingcloud.common.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSanitizerTest {

    @Test
    void nullInputShouldReturnNull() {
        assertNull(DataSanitizer.sanitize(null));
    }

    @Test
    void safeInputShouldPassCheck() {
        assertTrue(DataSanitizer.isSafe("hello world"));
        assertTrue(DataSanitizer.isSafe(null));
    }

    @Test
    void sqlKeywordShouldBeDetectedAsUnsafe() {
        assertFalse(DataSanitizer.isSafe("SELECT * FROM users"));
        assertFalse(DataSanitizer.isSafe("DROP TABLE users"));
        assertFalse(DataSanitizer.isSafe("INSERT INTO users"));
        assertFalse(DataSanitizer.isSafe("UPDATE users SET"));
        assertFalse(DataSanitizer.isSafe("DELETE FROM users"));
    }

    @Test
    void sqlInjectionPatternShouldBeDetected() {
        assertFalse(DataSanitizer.isSafe("1' OR '1'='1"));
        assertFalse(DataSanitizer.isSafe("1; DROP TABLE users; --"));
    }

    @Test
    void sanitizeShouldRemoveAngleBrackets() {
        assertEquals("scriptalert1/script", DataSanitizer.sanitize("<script>alert(1)</script>"));
    }

    @Test
    void sanitizeShouldRemoveQuotes() {
        assertEquals("hello world", DataSanitizer.sanitize("hello'\" world"));
    }

    @Test
    void sanitizeShouldRemoveSpecialChars() {
        assertEquals("hello world", DataSanitizer.sanitize("hello()&%;+ world"));
    }

    @Test
    void sanitizeShouldCollapseWhitespace() {
        assertEquals("hello world", DataSanitizer.sanitize("hello   \t  world"));
    }

    @Test
    void sanitizeShouldTrim() {
        assertEquals("hello", DataSanitizer.sanitize("  hello  "));
    }

    @Test
    void truncateShouldLimitLength() {
        assertEquals("hello", DataSanitizer.truncate("hello world", 5));
    }

    @Test
    void truncateShouldReturnIfShorterThanMax() {
        assertEquals("hi", DataSanitizer.truncate("hi", 10));
    }

    @Test
    void truncateNullShouldReturnNull() {
        assertNull(DataSanitizer.truncate(null, 10));
    }
}
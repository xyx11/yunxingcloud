package com.yunxingcloud.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    void okWithoutDataShouldReturn200() {
        R<Void> r = R.ok();
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void okWithDataShouldReturn200AndData() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
    }

    @Test
    void okWithMessageAndDataShouldReturn200() {
        R<Integer> r = R.ok("done", 42);
        assertEquals(200, r.getCode());
        assertEquals("done", r.getMessage());
        assertEquals(42, r.getData());
    }

    @Test
    void okWithComplexDataShouldWork() {
        R<Map<String, Object>> r = R.ok(Map.of("id", 1, "name", "test"));
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals(1, r.getData().get("id"));
    }

    @Test
    void failWithoutMessageShouldReturn500() {
        R<Void> r = R.fail();
        assertEquals(500, r.getCode());
        assertEquals("fail", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void failWithMessageShouldReturn500() {
        R<Void> r = R.fail("error occurred");
        assertEquals(500, r.getCode());
        assertEquals("error occurred", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void failWithCodeAndMessageShouldReturnCustomCode() {
        R<Void> r = R.fail(404, "not found");
        assertEquals(404, r.getCode());
        assertEquals("not found", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void setterAndGetterShouldWork() {
        R<List<String>> r = new R<>();
        r.setCode(201);
        r.setMessage("created");
        r.setData(List.of("a", "b"));
        assertEquals(201, r.getCode());
        assertEquals("created", r.getMessage());
        assertEquals(2, r.getData().size());
    }
}
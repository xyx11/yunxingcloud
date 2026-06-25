package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class SystemControllerTest extends BaseControllerTest {

    @Test
    void shouldGetSystemInfo() throws Exception {
        HttpRequest req = authRequest("/api/system/info").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldGetSessions() throws Exception {
        HttpRequest req = authRequest("/api/system/sessions").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }
}

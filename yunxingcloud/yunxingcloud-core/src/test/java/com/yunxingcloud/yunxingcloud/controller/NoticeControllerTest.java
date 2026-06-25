package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class NoticeControllerTest extends BaseControllerTest {

    @Test
    void shouldListNotices() throws Exception {
        HttpRequest req = authRequest("/api/notices").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldGetLatestNotices() throws Exception {
        HttpRequest req = authRequest("/api/notices/latest").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldRequireAuth() throws Exception {
        HttpRequest req = unauthRequest("/api/notices").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class PostControllerTest extends BaseControllerTest {

    @Test
    void shouldListPosts() throws Exception {
        HttpRequest req = authRequest("/api/posts").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldRequireAuth() throws Exception {
        HttpRequest req = unauthRequest("/api/posts").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest extends BaseControllerTest {

    @Test
    void shouldListUsersWithAuth() throws Exception {
        HttpRequest req = authRequest("/api/users").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldRejectUserListWithoutAuth() throws Exception {
        HttpRequest req = unauthRequest("/api/users").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldAccessUsersEndpoint() throws Exception {
        HttpRequest req = authRequest("/api/users?page=1&pageSize=5").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldRequireAuthForProtectedEndpoints() throws Exception {
        // users endpoint should require authentication
        HttpRequest req = unauthRequest("/api/users").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

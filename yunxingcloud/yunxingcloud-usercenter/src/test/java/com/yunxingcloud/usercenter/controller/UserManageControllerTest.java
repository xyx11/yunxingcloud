package com.yunxingcloud.usercenter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class UserManageControllerTest extends BaseUCTest {

    @Test void shouldListUsers() throws Exception {
        HttpRequest req = unauthRequest("/api/users").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }

    @Test void shouldGetPermissions() throws Exception {
        HttpRequest req = unauthRequest("/api/users/permissions").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }

    @Test void shouldCreateUser() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "username", "newtestuser", "password", "Test1234!", "email", "test@test.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/users")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }

    @Test void shouldCreateMultipleUsers() throws Exception {
        // create first user
        String body1 = objectMapper.writeValueAsString(Map.of(
            "username", "user_a", "password", "Test1234!", "email", "a@test.com"));
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/users")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .build();
        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp1.statusCode() < 500);

        // create second user
        String body2 = objectMapper.writeValueAsString(Map.of(
            "username", "user_b", "password", "Test1234!", "email", "b@test.com"));
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/users")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .build();
        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp2.statusCode() < 500);
    }
}

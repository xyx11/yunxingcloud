package com.yunxingcloud.yunxingcloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PasswordControllerTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void shouldReturn400WhenForgotPasswordWithBlankEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", ""));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/password/forgot")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shouldReturnOkWhenForgotPasswordWithNonexistentEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "noone@nowhere.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/password/forgot")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> data = objectMapper.readValue(resp.body(), Map.class);
        assertEquals(true, data.get("success"));
    }

    @Test
    void shouldReturn400WhenResetPasswordWithBlankToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("token", "", "newPassword", "NewPass1!"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/password/reset")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shouldReturn400WhenResetPasswordWithWeakPassword() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("token", "some-token", "newPassword", "123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/password/reset")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shouldRequireAuthForChangePassword() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "oldPassword", "admin123", "newPassword", "NewPass1!"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/password/change")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() == 401 || resp.statusCode() == 403);
    }
}
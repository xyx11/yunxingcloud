package com.yunxingcloud.usercenter.controller;

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
class RegisterControllerTest {

    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private String url(String path) { return "http://localhost:" + port + path; }

    @Test
    void shouldReturn400WhenUsernameBlank() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "", "password", "Test1234!", "email", "a@b.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        assertEquals(400, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldReturn400WhenPasswordWeak() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "newuser", "password", "123", "email", "a@b.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        assertEquals(400, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "username", "testreg" + System.currentTimeMillis() % 100000,
                "password", "Test1234!",
                "email", "test@example.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> data = mapper.readValue(resp.body(), Map.class);
        assertEquals(true, data.get("success"));
    }

    @Test
    void shouldReturn400WhenDuplicateUsername() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "username", "admin", "password", "Test1234!", "email", "a@b.com"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        assertEquals(400, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
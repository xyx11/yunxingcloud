package com.yunxingcloud.yunxingcloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MenuControllerTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private String token;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void login() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String resp = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        token = objectMapper.readTree(resp).get("accessToken").asText();
    }

    @Test
    void shouldReturnMenuTree() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/menus/tree")))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<?> data = objectMapper.readValue(resp.body(), List.class);
        assertFalse(data.isEmpty());
    }

    @Test
    void shouldReturnAllMenus() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/menus")))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<?> data = objectMapper.readValue(resp.body(), List.class);
        assertFalse(data.isEmpty());
    }

    @Test
    void shouldCreateAndDeleteMenu() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "测试菜单", "menuType", "C",
                "path", "/test", "sortOrder", 99, "visible", true
        ));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/menus")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String createResp = client.send(createReq, HttpResponse.BodyHandlers.ofString()).body();
        assertEquals(200, client.send(createReq, HttpResponse.BodyHandlers.ofString()).statusCode());
        int id = objectMapper.readTree(createResp).get("id").asInt();

        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/menus/" + id)))
                .header("Authorization", "Bearer " + token)
                .DELETE().build();
        assertEquals(200, client.send(deleteReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url("/api/menus"))).GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

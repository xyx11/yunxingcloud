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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StatsControllerTest {

    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private String token;

    private String url(String path) { return "http://localhost:" + port + path; }

    @BeforeEach
    void login() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        token = mapper.readTree(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .get("accessToken").asText();
    }

    @Test
    void shouldReturnDashboardStats() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/stats/dashboard")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> data = mapper.readValue(resp.body(), Map.class);
        assertNotNull(data.get("userCount"));
        assertNotNull(data.get("menuCount"));
        assertNotNull(data.get("weeklyOps"));
        assertNotNull(data.get("bizTypeDist"));
    }

    @Test
    void shouldHavePositiveCounts() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/stats/dashboard")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> data = mapper.readValue(resp.body(), Map.class);
        assertTrue(((Number) data.get("userCount")).intValue() >= 1);
        assertTrue(((Number) data.get("menuCount")).intValue() >= 1);
    }
}

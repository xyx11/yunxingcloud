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
class AuthControllerTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void shouldLoginSuccessfullyWithCorrectCredentials() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> data = objectMapper.readValue(resp.body(), Map.class);
        assertEquals(true, data.get("success"));
        assertEquals("admin", data.get("username"));
        assertNotNull(data.get("accessToken"));
        assertNotNull(data.get("refreshToken"));
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }

    @Test
    void shouldReturnUserInfoWithValidToken() throws Exception {
        // login
        String loginBody = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        String loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString()).body();
        String token = objectMapper.readTree(loginResp).get("accessToken").asText();

        // get user info
        HttpRequest userReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/user")))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> resp = client.send(userReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("admin", objectMapper.readTree(resp.body()).get("username").asText());
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/user"))).GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        String loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString()).body();
        String refreshToken = objectMapper.readTree(loginResp).get("refreshToken").asText();

        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        HttpRequest refreshReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/refresh")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(refreshBody))
                .build();
        HttpResponse<String> resp = client.send(refreshReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> data = objectMapper.readValue(resp.body(), Map.class);
        assertEquals(true, data.get("success"));
        assertNotNull(data.get("accessToken"));
    }

    @Test
    void shouldAllowPublicEndpointsWithoutAuth() throws Exception {
        HttpRequest captchaReq = HttpRequest.newBuilder().uri(URI.create(url("/api/captcha"))).GET().build();
        assertEquals(200, client.send(captchaReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest keyReq = HttpRequest.newBuilder().uri(URI.create(url("/api/publicKey"))).GET().build();
        assertEquals(200, client.send(keyReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest csrfReq = HttpRequest.newBuilder().uri(URI.create(url("/api/csrf"))).GET().build();
        assertEquals(200, client.send(csrfReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

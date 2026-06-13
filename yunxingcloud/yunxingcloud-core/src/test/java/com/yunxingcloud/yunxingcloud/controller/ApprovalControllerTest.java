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
class ApprovalControllerTest {

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
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Login failed with status " + resp.statusCode() + ": " + resp.body());
        }
        var node = mapper.readTree(resp.body());
        if (node.get("accessToken") == null) {
            throw new IllegalStateException("No accessToken in response: " + resp.body());
        }
        token = node.get("accessToken").asText();
    }

    @Test
    void shouldReturn401WithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/approval"))).GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldListMyApprovals() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/approval")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldListPendingApprovals() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/approval/pending")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldCreateAndApprove() throws Exception {
        // create
        String body = mapper.writeValueAsString(Map.of(
                "type", "leave", "title", "测试审批", "content", "测试内容"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/approval")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> createResp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResp.statusCode());
        int id = mapper.readTree(createResp.body()).get("id").asInt();

        // approve
        String approveBody = mapper.writeValueAsString(Map.of("remark", "同意"));
        HttpRequest approveReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/approval/" + id + "/approve")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(approveBody)).build();
        assertEquals(200, client.send(approveReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
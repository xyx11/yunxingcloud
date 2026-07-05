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
class TicketControllerTest {

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
        assertEquals(200, resp.statusCode(), "Login failed: " + resp.body());
        token = mapper.readTree(resp.body()).get("accessToken").asText();
    }

    @Test
    void shouldReturn401WithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets"))).GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldCreateTicket() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "title", "测试工单", "content", "测试内容",
                "type", "fault", "priority", "high"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        var node = mapper.readTree(resp.body());
        assertNotNull(node.get("id"));
        assertNotNull(node.get("ticketNo"));
        assertFalse(node.get("ticketNo").asText().isEmpty());
        assertEquals("测试工单", node.get("title").asText());
        assertEquals("0", node.get("status").asText());
    }

    @Test
    void shouldUpdateTicketStatus() throws Exception {
        // create
        String body = mapper.writeValueAsString(Map.of("title", "状态测试", "type", "other", "priority", "low"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        // update status
        String statusBody = mapper.writeValueAsString(Map.of("status", "1"));
        HttpRequest statusReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(statusBody)).build();
        assertEquals(200, client.send(statusReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // verify
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Authorization", "Bearer " + token).GET().build();
        assertEquals("1", mapper.readTree(client.send(getReq, HttpResponse.BodyHandlers.ofString()).body()).get("status").asText());
    }

    @Test
    void shouldAssignTicket() throws Exception {
        // create
        String body = mapper.writeValueAsString(Map.of("title", "指派测试", "type", "request", "priority", "medium"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        // assign
        String assignBody = mapper.writeValueAsString(Map.of("assignee", "zhangsan"));
        HttpRequest assignReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(assignBody)).build();
        assertEquals(200, client.send(assignReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldDeleteTicket() throws Exception {
        // create
        String body = mapper.writeValueAsString(Map.of("title", "删除测试", "type", "other", "priority", "low"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        // delete
        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Authorization", "Bearer " + token).DELETE().build();
        assertEquals(200, client.send(deleteReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // verify gone
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Authorization", "Bearer " + token).GET().build();
        assertEquals(404, client.send(getReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldNotUpdateClosedTicket() throws Exception {
        // create + close
        String body = mapper.writeValueAsString(Map.of("title", "关闭测试", "type", "other", "priority", "low"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        // close
        String closeBody = mapper.writeValueAsString(Map.of("status", "3"));
        HttpRequest closeReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(closeBody)).build();
        assertEquals(200, client.send(closeReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // try update closed
        String updateBody = mapper.writeValueAsString(Map.of("title", "尝试修改已关闭"));
        HttpRequest updateReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/tickets/" + id)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(updateBody)).build();
        assertEquals(400, client.send(updateReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

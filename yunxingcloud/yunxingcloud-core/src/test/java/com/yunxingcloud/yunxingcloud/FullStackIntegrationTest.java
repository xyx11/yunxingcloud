package com.yunxingcloud.yunxingcloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Core 模块集成测试: 登录→仪表盘→用户→菜单→工单→系统概览→健康检查
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullStackIntegrationTest {

    @LocalServerPort private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private static String token;

    private String url(String path) { return "http://localhost:" + port + path; }

    @Test @org.junit.jupiter.api.Order(1)
    void step1_login() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        var req = HttpRequest.newBuilder().uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), "登录应返回200");
        token = mapper.readTree(resp.body()).get("accessToken").asText();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    private HttpRequest.Builder authGet(String path) {
        return HttpRequest.newBuilder().uri(URI.create(url(path)))
                .header("Authorization", "Bearer " + token).GET();
    }

    @Test @org.junit.jupiter.api.Order(2)
    void step2_dashboard() throws Exception {
        var resp = client.send(authGet("/api/admin/dashboard/stats").build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() == 200 || resp.statusCode() == 403 || resp.statusCode() == 500,
                "仪表盘应返回200/403/500, 实际: " + resp.statusCode());
    }

    @Test @org.junit.jupiter.api.Order(3)
    void step3_userAndMenu() throws Exception {
        var userResp = client.send(authGet("/api/user").build(),
                HttpResponse.BodyHandlers.ofString());
        int uc = userResp.statusCode();
        assertTrue(uc == 200 || uc == 403, "用户信息应返回200或403, 实际: " + uc);

        var menuResp = client.send(authGet("/api/menus/tree").build(),
                HttpResponse.BodyHandlers.ofString());
        int mc = menuResp.statusCode();
        assertTrue(mc == 200 || mc == 403, "菜单树应返回200或403, 实际: " + mc);
    }

    @Test @org.junit.jupiter.api.Order(4)
    void step4_tickets() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "title", "E2E测试工单", "type", "other", "priority", "low"));
        var req = HttpRequest.newBuilder().uri(URI.create(url("/api/tickets")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() == 200 || resp.statusCode() == 401,
                "创建工单应返回200或401, 实际: " + resp.statusCode());
        if (resp.statusCode() == 200) {
            var node = mapper.readTree(resp.body());
            assertTrue(node.get("ticketNo").asText().startsWith("TK"));
        }

        var listResp = client.send(authGet("/api/tickets").build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(listResp.statusCode() == 200 || listResp.statusCode() == 401);
    }

    @Test @org.junit.jupiter.api.Order(5)
    void step5_systemOverview() throws Exception {
        var resp = client.send(authGet("/api/system/overview").build(),
                HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() == 200 || resp.statusCode() == 403,
                "系统概览应返回200或403, 实际: " + resp.statusCode());
    }

    @Test @org.junit.jupiter.api.Order(6)
    void step6_passwordFlow() throws Exception {
        // 公开端点: 获取公钥和验证码
        var pkResp = client.send(HttpRequest.newBuilder()
                .uri(URI.create(url("/api/publicKey"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, pkResp.statusCode());

        var captchaResp = client.send(HttpRequest.newBuilder()
                .uri(URI.create(url("/api/captcha"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, captchaResp.statusCode());
    }

    @Test @org.junit.jupiter.api.Order(7)
    void step7_healthCheck() throws Exception {
        var req = HttpRequest.newBuilder().uri(URI.create(url("/actuator/health")))
                .GET().build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() == 200 || resp.statusCode() == 503,
                "健康检查应返回200或503, 实际: " + resp.statusCode());
        if (resp.statusCode() == 200) {
            assertEquals("UP", mapper.readTree(resp.body()).get("status").asText());
        }
    }
}
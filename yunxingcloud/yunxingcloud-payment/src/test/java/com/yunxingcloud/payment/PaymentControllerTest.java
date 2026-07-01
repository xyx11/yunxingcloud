package com.yunxingcloud.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaymentControllerTest {

    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-for-payment-tests-min-256-bits".getBytes(StandardCharsets.UTF_8));
    private String token;

    private String url(String path) { return "http://localhost:" + port + path; }

    @BeforeEach
    void generateToken() {
        token = Jwts.builder()
                .subject("admin")
                .claim("authorities", "ticket:read,ticket:write,admin")
                .signWith(key).compact();
    }

    @Test
    void shouldCreatePaymentOrder() throws Exception {
        String body = mapper.writeValueAsString(Map.of("title", "Test Payment", "amount", 100, "channel", "wechat"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        var node = mapper.readTree(resp.body());
        assertNotNull(node.get("id"));
        assertNotNull(node.get("orderNo"));
        assertTrue(node.get("orderNo").asText().startsWith("PAY"));
        assertEquals("0", node.get("status").asText());
    }

    @Test
    void shouldPayOrder() throws Exception {
        String body = mapper.writeValueAsString(Map.of("title", "Pay Test", "amount", 200, "channel", "alipay"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        HttpRequest payReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders/" + id + "/pay")))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(200, client.send(payReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders/" + id)))
                .header("Authorization", "Bearer " + token).GET().build();
        assertEquals("1", mapper.readTree(client.send(getReq, HttpResponse.BodyHandlers.ofString()).body()).get("status").asText());
    }

    @Test
    void shouldListOrders() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldReturn401WithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders"))).GET().build();
        int code = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
        assertTrue(code == 401 || code == 403, "无认证应返回401或403, 实际: " + code);
    }

    @Test
    void shouldQueryOrder() throws Exception {
        String body = mapper.writeValueAsString(Map.of("title", "Query Test", "amount", 50, "channel", "wechat"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int id = mapper.readTree(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/payment/orders/" + id)))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals(id, mapper.readTree(resp.body()).get("id").asInt());
    }
}
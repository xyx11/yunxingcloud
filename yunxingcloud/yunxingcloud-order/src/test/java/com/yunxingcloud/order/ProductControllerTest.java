package com.yunxingcloud.order;

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
class ProductControllerTest {

    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-for-order-tests-min-256-bits".getBytes(StandardCharsets.UTF_8));
    private String token;

    private String url(String path) { return "http://localhost:" + port + path; }

    @BeforeEach
    void generateToken() {
        token = Jwts.builder()
                .subject("admin")
                .claim("authorities", "admin,ticket:write")
                .signWith(key).compact();
    }

    @Test
    void shouldListProductsWithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products"))).GET().build();
        assertEquals(200, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldSearchProducts() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products/search?q=test")))
                .header("Authorization", "Bearer " + token).GET().build();
        int code = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
        assertTrue(code == 200 || code == 403, "搜索应返回200或403, 实际: " + code);
    }

    @Test
    void shouldCreateProductWithAuth() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "name", "Test Product", "description", "Test desc",
                "price", 9900, "stock", 100, "status", "0"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        var node = mapper.readTree(resp.body());
        assertNotNull(node.get("id"));
        assertEquals("Test Product", node.get("name").asText());
    }

    @Test
    void shouldNotCreateProductWithoutAuth() throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", "Unauthorized", "price", 100, "stock", 1));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int code = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
        assertTrue(code == 401 || code == 403, "无认证POST应返回401或403, 实际: " + code);
    }

    @Test
    void shouldGetHotAndNewProducts() throws Exception {
        HttpRequest hotReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products/hot"))).GET().build();
        assertEquals(200, client.send(hotReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest newReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/products/new"))).GET().build();
        assertEquals(200, client.send(newReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldCreateCategory() throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", "Electronics", "description", "Electronic items"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/categories")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertNotNull(mapper.readTree(resp.body()).get("id"));
    }
}
package com.yunxingcloud.inventory;

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
class InventoryControllerTest {

    @LocalServerPort
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-for-inventory-tests-min-256-bits".getBytes(StandardCharsets.UTF_8));
    private String token;

    private String url(String path) { return "http://localhost:" + port + path; }

    @BeforeEach
    void generateToken() {
        token = Jwts.builder()
                .subject("admin")
                .claim("authorities", "admin")
                .signWith(key).compact();
    }

    @Test
    void shouldListEmptyWarehouses() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/warehouses")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldAddWarehouse() throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", "Default Warehouse", "address", "123 Test St"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/warehouses")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertNotNull(mapper.readTree(resp.body()).get("id"));
    }

    @Test
    void shouldListEmptyInventory() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/inventory")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldStockIn() throws Exception {
        String whBody = mapper.writeValueAsString(Map.of("name", "WH1", "address", "addr"));
        HttpRequest whReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/warehouses")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(whBody)).build();
        int whId = mapper.readTree(client.send(whReq, HttpResponse.BodyHandlers.ofString()).body()).get("id").asInt();

        String body = mapper.writeValueAsString(Map.of("productId", 1, "productName", "Test Item",
                "warehouseId", whId, "quantity", 50, "remark", "init stock"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/inventory/stock-in")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals(50, mapper.readTree(resp.body()).get("quantity").asInt());
    }

    @Test
    void shouldReturn401WithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/inventory"))).GET().build();
        int code = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
        assertTrue(code == 401 || code == 403, "无认证应返回401或403, 实际: " + code);
    }

    @Test
    void shouldGetEmptyAlerts() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/inventory/alerts")))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(mapper.readTree(resp.body()).isArray());
    }

    @Test
    void shouldAllowOrderOutWithoutAuth() throws Exception {
        String body = mapper.writeValueAsString(Map.of("productId", 1, "productName", "Item",
                "warehouseId", 1, "quantity", 1, "orderId", 100));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/inventory/order-out")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int status = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }
}
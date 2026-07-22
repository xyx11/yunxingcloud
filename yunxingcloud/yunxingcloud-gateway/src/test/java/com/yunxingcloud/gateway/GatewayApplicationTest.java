package com.yunxingcloud.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayApplicationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldLoadContext() {
        assertThat(port).isGreaterThan(0);
        assertThat(webClient).isNotNull();
    }

    @Test
    void healthEndpointShouldBeAccessible() {
        webClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void publicGetPathShouldNotRequireAuth() {
        // Products is GET-only public - passes auth filter
        webClient.get().uri("/api/products/1")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void protectedOrderPathShouldReturn401WithoutToken() {
        webClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedOrderPathShouldReturn401WithInvalidToken() {
        webClient.get().uri("/api/orders")
                .header("Authorization", "Bearer invalid-token-here")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void validTokenShouldPassAuthFilter() {
        String token = generateToken("testuser");
        webClient.get().uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is2xxSuccessful(); // Passes auth, backend may return empty/200
    }

    @Test
    void cartPathShouldRequireAuth() {
        webClient.get().uri("/api/cart")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void profilePathShouldRequireAuth() {
        webClient.get().uri("/api/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void postToProtectedPathShouldReturn401() {
        webClient.post().uri("/api/orders")
                .bodyValue("{}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void expiredTokenShouldReturn401() {
        String token = generateExpiredToken("testuser");
        webClient.get().uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .claim("authorities", "ROLE_USER")
                .signWith(key)
                .compact();
    }

    private String generateExpiredToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .claim("authorities", "ROLE_USER")
                .signWith(key)
                .compact();
    }

    private static final String SECRET = "yunxingcloud-jwt-secret-key-2024";
}

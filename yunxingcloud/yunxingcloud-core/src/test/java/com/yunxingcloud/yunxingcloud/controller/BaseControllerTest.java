package com.yunxingcloud.yunxingcloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @LocalServerPort
    protected int port;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final HttpClient client = HttpClient.newHttpClient();

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String loginAsAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String resp = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        return objectMapper.readTree(resp).get("accessToken").asText();
    }

    protected HttpRequest.Builder authRequest(String path) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .header("Authorization", "Bearer " + loginAsAdmin());
    }

    protected HttpRequest.Builder unauthRequest(String path) {
        return HttpRequest.newBuilder().uri(URI.create(url(path)));
    }
}

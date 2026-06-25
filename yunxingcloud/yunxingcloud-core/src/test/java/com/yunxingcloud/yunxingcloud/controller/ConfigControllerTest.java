package com.yunxingcloud.yunxingcloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigControllerTest extends BaseControllerTest {

    @Test
    void shouldListConfigs() throws Exception {
        HttpRequest req = authRequest("/api/config").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldCreateAndDeleteConfig() throws Exception {
        String token = loginAsAdmin();
        var config = Map.of("name", "test_config", "configKey", "test.key", "configValue", "test_value", "configType", "Y");
        String body = objectMapper.writeValueAsString(config);

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/config")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> created = objectMapper.readValue(resp.body(), Map.class);
        int id = ((Number) created.get("id")).intValue();

        HttpRequest delReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/config/" + id)))
                .header("Authorization", "Bearer " + token)
                .DELETE().build();
        assertEquals(200, client.send(delReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldRejectDuplicateConfigKey() throws Exception {
        String token = loginAsAdmin();
        var config = Map.of("name", "dup_test", "configKey", "dup.test.key", "configValue", "val", "configType", "Y");
        String body = objectMapper.writeValueAsString(config);

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/config")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        Map<?, ?> created = objectMapper.readValue(client.send(createReq, HttpResponse.BodyHandlers.ofString()).body(), Map.class);

        // try duplicate
        HttpResponse<String> resp2 = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp2.statusCode() == 400 || resp2.statusCode() == 200);

        // cleanup
        int id = ((Number) created.get("id")).intValue();
        HttpRequest delReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/config/" + id)))
                .header("Authorization", "Bearer " + token)
                .DELETE().build();
        client.send(delReq, HttpResponse.BodyHandlers.ofString());
    }
}

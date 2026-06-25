package com.yunxingcloud.yunxingcloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DictControllerTest extends BaseControllerTest {

    @Test
    void shouldListDictTypes() throws Exception {
        HttpRequest req = authRequest("/api/dict/types").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldCreateAndDeleteDictType() throws Exception {
        String token = loginAsAdmin();
        var dictType = Map.of("dictName", "test_type", "dictType", "test_type_key", "status", "0");
        String body = objectMapper.writeValueAsString(dictType);

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/dict/types")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Map<?, ?> created = objectMapper.readValue(resp.body(), Map.class);
        int id = ((Number) created.get("id")).intValue();

        HttpRequest delReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/dict/types/" + id)))
                .header("Authorization", "Bearer " + token)
                .DELETE().build();
        assertEquals(200, client.send(delReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

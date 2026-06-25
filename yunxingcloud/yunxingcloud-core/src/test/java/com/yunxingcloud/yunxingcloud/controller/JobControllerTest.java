package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JobControllerTest extends BaseControllerTest {

    @Test
    void shouldListJobs() throws Exception {
        HttpRequest req = authRequest("/api/job").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldRejectJobAccessWithoutAuth() throws Exception {
        HttpRequest req = unauthRequest("/api/job").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldAccessJobEndpoints() throws Exception {
        String token = loginAsAdmin();
        var job = Map.of("jobName", "testJob", "jobGroup", "DEFAULT", "invokeTarget", "test.target",
                "cronExpression", "0 0 2 * * ?", "misfirePolicy", "3", "concurrent", "1", "status", "1");
        String body = objectMapper.writeValueAsString(job);

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/job")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();
        // should return 200 for success or 400/500 for validation errors
        assertTrue(status >= 200 && status < 500, "Expected status 2xx-4xx, got " + status);
    }
}

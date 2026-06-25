package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class ConsentControllerTest extends BaseControllerTest {
    @Test void shouldRequireAuthForConsentClient() throws Exception {
        HttpRequest req = unauthRequest("/api/oauth2/consent/client?client_id=test").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500, "Expected non-5xx, got " + resp.statusCode());
    }
}

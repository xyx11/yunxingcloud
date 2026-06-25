package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2ClientControllerTest extends BaseControllerTest {
    @Test void shouldListClients() throws Exception {
        HttpRequest req = authRequest("/api/oauth2/clients").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }
    @Test void shouldRejectUnauthorized() throws Exception {
        HttpRequest req = unauthRequest("/api/oauth2/clients").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

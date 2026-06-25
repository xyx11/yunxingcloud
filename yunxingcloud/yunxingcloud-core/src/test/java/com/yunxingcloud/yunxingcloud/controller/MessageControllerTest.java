package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class MessageControllerTest extends BaseControllerTest {
    @Test void shouldAccessInbox() throws Exception {
        HttpRequest req = authRequest("/api/messages/inbox").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }
    @Test void shouldRejectUnauthorized() throws Exception {
        HttpRequest req = unauthRequest("/api/messages/inbox").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

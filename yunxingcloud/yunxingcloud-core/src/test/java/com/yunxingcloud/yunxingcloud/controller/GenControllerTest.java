package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class GenControllerTest extends BaseControllerTest {
    @Test void shouldRejectUnauthorized() throws Exception {
        HttpRequest req = unauthRequest("/api/generator/tables").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

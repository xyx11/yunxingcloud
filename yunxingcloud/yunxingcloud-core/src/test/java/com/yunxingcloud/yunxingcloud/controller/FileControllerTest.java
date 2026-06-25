package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class FileControllerTest extends BaseControllerTest {
    @Test void shouldRejectUnauthorized() throws Exception {
        HttpRequest req = unauthRequest("/api/files").GET().build();
        assertEquals(401, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

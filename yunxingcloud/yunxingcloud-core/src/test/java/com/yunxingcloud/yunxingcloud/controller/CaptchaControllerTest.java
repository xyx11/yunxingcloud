package com.yunxingcloud.yunxingcloud.controller;

import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaControllerTest extends BaseControllerTest {

    @Test
    void shouldGetCaptcha() throws Exception {
        HttpRequest req = unauthRequest("/api/captcha").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void shouldGetPublicKey() throws Exception {
        HttpRequest req = unauthRequest("/api/publicKey").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }
}

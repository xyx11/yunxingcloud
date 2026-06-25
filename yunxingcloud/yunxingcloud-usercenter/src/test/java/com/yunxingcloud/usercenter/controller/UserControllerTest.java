package com.yunxingcloud.usercenter.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest extends BaseUCTest {

    @Test void shouldGetUserInfo() throws Exception {
        HttpRequest req = unauthRequest("/api/user").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }

    @Test void shouldGetSocialAccounts() throws Exception {
        HttpRequest req = unauthRequest("/api/user/social").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }
}

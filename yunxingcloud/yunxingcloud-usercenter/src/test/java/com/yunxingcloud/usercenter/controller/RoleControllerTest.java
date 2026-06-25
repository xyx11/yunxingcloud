package com.yunxingcloud.usercenter.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class RoleControllerTest extends BaseUCTest {
    @Test void shouldListRoles() throws Exception {
        HttpRequest req = unauthRequest("/api/roles").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }
}

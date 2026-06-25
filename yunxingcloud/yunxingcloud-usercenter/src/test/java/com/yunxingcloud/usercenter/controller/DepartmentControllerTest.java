package com.yunxingcloud.usercenter.controller;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentControllerTest extends BaseUCTest {
    @Test void shouldListDepartments() throws Exception {
        HttpRequest req = unauthRequest("/api/departments").GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() < 500);
    }
}

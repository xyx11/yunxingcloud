package com.yunxingcloud.usercenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsercenterApplicationTests {

    @LocalServerPort
    private int port;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private String sessionCookie;

    private String url(String path) { return "http://localhost:" + port + path; }

    @BeforeEach
    void login() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        sessionCookie = resp.headers().firstValue("Set-Cookie").orElse("");
    }

    @Test
    void contextLoads() {}

    @Test
    void shouldListUsers() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/users")))
                .header("Cookie", sessionCookie)
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<?> users = mapper.readValue(resp.body(), List.class);
        assertFalse(users.isEmpty());
    }

    @Test
    void shouldListRoles() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/roles")))
                .header("Cookie", sessionCookie)
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<?> roles = mapper.readValue(resp.body(), List.class);
        assertTrue(roles.size() >= 2); // admin + user roles seeded
    }

    @Test
    void shouldGetDepartmentTree() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/departments")))
                .header("Cookie", sessionCookie)
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<?> depts = mapper.readValue(resp.body(), List.class);
        assertFalse(depts.isEmpty());
    }

    @Test
    void shouldCreateAndDeleteRole() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "name", "测试角色", "code", "test_role", "description", "测试", "permissions", "user:read"));
        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/roles")))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> createResp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResp.statusCode());
        int id = mapper.readTree(createResp.body()).get("id").asInt();

        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/roles/" + id)))
                .header("Cookie", sessionCookie)
                .DELETE().build();
        assertEquals(200, client.send(deleteReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldAssignRoleToUser() throws Exception {
        // get first role id
        HttpRequest rolesReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/roles")))
                .header("Cookie", sessionCookie).GET().build();
        List<Map> roles = mapper.readValue(
                client.send(rolesReq, HttpResponse.BodyHandlers.ofString()).body(), List.class);
        int roleId = (int) roles.get(0).get("id");

        // assign to admin user
        String body = mapper.writeValueAsString(Map.of("roleIds", List.of(roleId)));
        HttpRequest assignReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/users/1/roles")))
                .header("Content-Type", "application/json")
                .header("Cookie", sessionCookie)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        assertEquals(200, client.send(assignReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void shouldRedirectToLoginWithoutAuth() throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url("/api/users"))).GET().build();
        assertEquals(302, client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}

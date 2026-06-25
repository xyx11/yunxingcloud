package com.yunxingcloud.usercenter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseUCTest {
    @LocalServerPort protected int port;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final HttpClient client = HttpClient.newHttpClient();

    protected String url(String path) { return "http://localhost:" + port + path; }

    protected HttpRequest.Builder unauthRequest(String path) {
        return HttpRequest.newBuilder().uri(URI.create(url(path)));
    }
}

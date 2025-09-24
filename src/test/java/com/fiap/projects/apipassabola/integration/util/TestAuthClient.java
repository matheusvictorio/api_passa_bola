package com.fiap.projects.apipassabola.integration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

public class TestAuthClient {

    private final TestRestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public TestAuthClient(TestRestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public AuthResult registerPlayer(String username, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", "Password123!");
        body.put("name", "Test Player");
        body.put("bio", "bio");
        body.put("profilePhotoUrl", null);
        body.put("bannerUrl", null);
        body.put("phone", "");
        body.put("gamesPlayed", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/api/auth/register/player",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        return extractAuthResult(resp);
    }

    public AuthResult registerOrganization(String username, String email, String cnpj) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", "Password123!");
        body.put("name", "Test Org");
        body.put("cnpj", cnpj);
        body.put("bio", "bio");
        body.put("profilePhotoUrl", null);
        body.put("bannerUrl", null);
        body.put("phone", "");
        body.put("city", "Sao Paulo");
        body.put("state", "SP");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/api/auth/register/organization",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        return extractAuthResult(resp);
    }

    public AuthResult registerSpectator(String username, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", "Password123!");
        body.put("name", "Test Spectator");
        body.put("bio", "bio");
        body.put("profilePhotoUrl", null);
        body.put("bannerUrl", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/api/auth/register/spectator",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        return extractAuthResult(resp);
    }

    public AuthResult login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        return extractAuthResult(resp);
    }

    private AuthResult extractAuthResult(ResponseEntity<Map> resp) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Auth call failed: " + resp.getStatusCode());
        }
        Map<String, Object> map = resp.getBody();
        String token = (String) map.get("token");
        Number userId = (Number) map.get("userId");
        String username = (String) map.get("username");
        String email = (String) map.get("email");
        return new AuthResult(token, userId != null ? userId.longValue() : null, username, email);
    }

    public record AuthResult(String token, Long userId, String username, String email) {}
}

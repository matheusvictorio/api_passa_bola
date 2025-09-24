package com.fiap.projects.apipassabola.integration;

import com.fiap.projects.apipassabola.integration.util.TestAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrganizationIntegrationTest extends BaseIntegrationTest {

    @Test
    void list_and_get_organization() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);
        var org = auth.registerOrganization("orgt_" + u, "orgt_" + u + "@email.com", "04252011000110");

        HttpHeaders headers = authHeaders(org.token());

        // GET by id
        ResponseEntity<Map> byId = restTemplate.exchange(
                baseUrl("/api/organizations/" + org.userId()),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        assertEquals(HttpStatus.OK, byId.getStatusCode());
        assertEquals(org.userId().longValue(), ((Number) byId.getBody().get("id")).longValue());

        // GET by username
        String username = (String) byId.getBody().get("username");
        ResponseEntity<Map> byUsername = restTemplate.exchange(
                baseUrl("/api/organizations/username/" + username),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        assertEquals(HttpStatus.OK, byUsername.getStatusCode());

        // LIST
        ResponseEntity<Object> list = restTemplate.exchange(
                baseUrl("/api/organizations"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, list.getStatusCode());

        // SEARCH
        ResponseEntity<Object> search = restTemplate.exchange(
                baseUrl("/api/organizations/search?name=" + byId.getBody().get("name")),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, search.getStatusCode());
    }
}

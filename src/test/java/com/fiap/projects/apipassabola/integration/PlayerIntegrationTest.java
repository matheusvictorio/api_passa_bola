package com.fiap.projects.apipassabola.integration;

import com.fiap.projects.apipassabola.integration.util.TestAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerIntegrationTest extends BaseIntegrationTest {

    @Test
    void list_and_get_player() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);
        var pl = auth.registerPlayer("pl_" + u, "pl_" + u + "@email.com");

        HttpHeaders headers = authHeaders(pl.token());

        // GET by id
        ResponseEntity<Map> byId = restTemplate.exchange(
                baseUrl("/api/players/" + pl.userId()),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        assertEquals(HttpStatus.OK, byId.getStatusCode());

        // GET by username
        String username = (String) byId.getBody().get("username");
        ResponseEntity<Map> byUsername = restTemplate.exchange(
                baseUrl("/api/players/username/" + username),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        assertEquals(HttpStatus.OK, byUsername.getStatusCode());

        // LIST
        ResponseEntity<Object> list = restTemplate.exchange(
                baseUrl("/api/players"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, list.getStatusCode());

        // SEARCH
        ResponseEntity<Object> search = restTemplate.exchange(
                baseUrl("/api/players/search?name=" + byId.getBody().get("name")),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, search.getStatusCode());
    }
}

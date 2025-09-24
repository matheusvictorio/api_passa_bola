package com.fiap.projects.apipassabola.integration;

import com.fiap.projects.apipassabola.integration.util.TestAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PostIntegrationTest extends BaseIntegrationTest {

    @Test
    void create_get_like_unlike_post_flow() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);
        var player = auth.registerPlayer("postp_" + u, "postp_" + u + "@email.com");

        // Create post
        Map<String, Object> postReq = new HashMap<>();
        postReq.put("content", "Hello world " + u);
        postReq.put("imageUrl", null);
        postReq.put("type", "GENERAL");

        HttpHeaders headers = authHeaders(player.token());
        ResponseEntity<Map> createResp = restTemplate.exchange(
                baseUrl("/api/posts"),
                HttpMethod.POST,
                new HttpEntity<>(postReq, headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        Number idNum = (Number) createResp.getBody().get("id");
        Long postId = idNum.longValue();
        assertNotNull(postId);

        // Get post by id
        ResponseEntity<Map> getResp = restTemplate.exchange(
                baseUrl("/api/posts/" + postId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals(postId.longValue(), ((Number) getResp.getBody().get("id")).longValue());

        // Like
        ResponseEntity<Map> likeResp = restTemplate.exchange(
                baseUrl("/api/posts/" + postId + "/like"),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );
        assertTrue(likeResp.getStatusCode().is2xxSuccessful());

        // Liked?
        ResponseEntity<Map> likedResp = restTemplate.exchange(
                baseUrl("/api/posts/" + postId + "/liked"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(Boolean.TRUE, likedResp.getBody().get("hasLiked"));

        // Unlike
        ResponseEntity<Void> unlikeResp = restTemplate.exchange(
                baseUrl("/api/posts/" + postId + "/like"),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, unlikeResp.getStatusCode());
    }
}

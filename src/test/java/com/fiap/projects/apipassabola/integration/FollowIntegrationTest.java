package com.fiap.projects.apipassabola.integration;

import com.fiap.projects.apipassabola.integration.util.TestAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FollowIntegrationTest extends BaseIntegrationTest {

    private Map<String, Object> followBody(Long targetId, String targetType) {
        Map<String, Object> body = new HashMap<>();
        body.put("targetUserId", targetId);
        body.put("targetUserType", targetType);
        return body;
    }

    @Test
    void follow_and_lists_work() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);

        var p1 = auth.registerPlayer("p1_" + u, "p1_" + u + "@email.com");
        var p2 = auth.registerPlayer("p2_" + u, "p2_" + u + "@email.com");

        // p1 follows p2 (universal follow endpoint)
        Map<String, Object> body = followBody(p2.userId(), "PLAYER");

        HttpHeaders headers = authHeaders(p1.token());
        ResponseEntity<String> followResp = restTemplate.exchange(
                baseUrl("/api/follow"),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
        assertTrue(followResp.getStatusCode().is2xxSuccessful());

        // Check is-following
        ResponseEntity<Boolean> isFollowing = restTemplate.exchange(
                baseUrl("/api/follow/check"),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Boolean.class
        );
        assertEquals(Boolean.TRUE, isFollowing.getBody());

        // Public followers of p2
        ResponseEntity<Object> followers = restTemplate.exchange(
                baseUrl("/api/follow/followers/" + p2.userId() + "/PLAYER"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, followers.getStatusCode());

        // Public following of p1
        ResponseEntity<Object> following = restTemplate.exchange(
                baseUrl("/api/follow/following/" + p1.userId() + "/PLAYER"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertEquals(HttpStatus.OK, following.getStatusCode());

        // My following (p1)
        ResponseEntity<Object> myFollowing = restTemplate.exchange(
                baseUrl("/api/follow/my-following"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );
        assertEquals(HttpStatus.OK, myFollowing.getStatusCode());

        // Unfollow
        ResponseEntity<String> unfollowResp = restTemplate.exchange(
                baseUrl("/api/follow"),
                HttpMethod.DELETE,
                new HttpEntity<>(body, headers),
                String.class
        );
        assertTrue(unfollowResp.getStatusCode().is2xxSuccessful());

        // Check is-following -> false
        ResponseEntity<Boolean> isFollowingAfter = restTemplate.exchange(
                baseUrl("/api/follow/check"),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Boolean.class
        );
        assertEquals(Boolean.FALSE, isFollowingAfter.getBody());
    }

    @Test
    void cross_type_follows_player_to_spectator_and_organization() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);

        var player = auth.registerPlayer("plx_" + u, "plx_" + u + "@email.com");
        var spectator = auth.registerSpectator("spx_" + u, "spx_" + u + "@email.com");
        var org = auth.registerOrganization("orgx_" + u, "orgx_" + u + "@email.com", "04252011000110");

        HttpHeaders pHeaders = authHeaders(player.token());

        // Player follows Spectator
        ResponseEntity<String> f1 = restTemplate.exchange(
                baseUrl("/api/follow"),
                HttpMethod.POST,
                new HttpEntity<>(followBody(spectator.userId(), "SPECTATOR"), pHeaders),
                String.class
        );
        assertTrue(f1.getStatusCode().is2xxSuccessful());

        // Player follows Organization
        ResponseEntity<String> f2 = restTemplate.exchange(
                baseUrl("/api/follow"),
                HttpMethod.POST,
                new HttpEntity<>(followBody(org.userId(), "ORGANIZATION"), pHeaders),
                String.class
        );
        assertTrue(f2.getStatusCode().is2xxSuccessful());

        // List following for player (should be OK)
        ResponseEntity<Object> myFollowing = restTemplate.exchange(
                baseUrl("/api/follow/my-following"),
                HttpMethod.GET,
                new HttpEntity<>(pHeaders),
                Object.class
        );
        assertEquals(HttpStatus.OK, myFollowing.getStatusCode());
    }

    @Test
    void check_not_following_returns_false() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String u = UUID.randomUUID().toString().substring(0,8);
        var p1 = auth.registerPlayer("p1nf_" + u, "p1nf_" + u + "@email.com");
        var p2 = auth.registerPlayer("p2nf_" + u, "p2nf_" + u + "@email.com");

        HttpHeaders headers = authHeaders(p1.token());
        ResponseEntity<Boolean> isFollowing = restTemplate.exchange(
                baseUrl("/api/follow/check"),
                HttpMethod.POST,
                new HttpEntity<>(followBody(p2.userId(), "PLAYER"), headers),
                Boolean.class
        );
        assertEquals(Boolean.FALSE, isFollowing.getBody());

        // My followers should be 200 even if empty
        ResponseEntity<Object> myFollowers = restTemplate.exchange(
                baseUrl("/api/follow/my-followers"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );
        assertEquals(HttpStatus.OK, myFollowers.getStatusCode());
    }
}

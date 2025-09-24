package com.fiap.projects.apipassabola.integration;

import com.fiap.projects.apipassabola.integration.util.TestAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void registerAndLoginPlayer_success() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String unique = UUID.randomUUID().toString().substring(0,8);
        var reg = auth.registerPlayer("player_" + unique, "player_" + unique + "@email.com");
        assertNotNull(reg.token());
        assertNotNull(reg.userId());

        var login = auth.login(reg.email(), "Password123!");
        assertNotNull(login.token());
        assertEquals(reg.userId(), login.userId());
    }

    @Test
    void registerOrganization_success() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String unique = UUID.randomUUID().toString().substring(0,8);
        // Valid but simple CNPJ digits (not real) just to pass formatting checks if any persistence only
        var org = auth.registerOrganization("org_" + unique, "org_" + unique + "@email.com", "11222333000181");
        assertNotNull(org.token());
        assertNotNull(org.userId());
    }

    @Test
    void registerSpectator_success() {
        TestAuthClient auth = new TestAuthClient(restTemplate, "http://localhost:" + port);
        String unique = UUID.randomUUID().toString().substring(0,8);
        var spec = auth.registerSpectator("spec_" + unique, "spec_" + unique + "@email.com");
        assertNotNull(spec.token());
        assertNotNull(spec.userId());
    }
}

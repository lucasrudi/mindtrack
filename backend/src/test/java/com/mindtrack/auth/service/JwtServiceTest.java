package com.mindtrack.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long!",
                86400000L
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken(1L, "test@example.com", "USER");

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtService.generateToken(42L, "test@example.com", "USER");

        assertEquals(42L, jwtService.getUserIdFromToken(token));
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateToken(1L, "test@example.com", "USER");

        assertEquals("test@example.com", jwtService.getEmailFromToken(token));
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtService.generateToken(1L, "test@example.com", "ADMIN");

        assertEquals("ADMIN", jwtService.getRoleFromToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void shouldRejectNullToken() {
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService shortLived = new JwtService(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long!",
                -1000L
        );
        String token = shortLived.generateToken(1L, "test@example.com", "USER");

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectTokenWithDifferentKey() {
        JwtService otherService = new JwtService(
                "different-secret-key-for-testing-must-also-be-256-bits-long!!",
                86400000L
        );
        String token = otherService.generateToken(1L, "test@example.com", "USER");

        assertFalse(jwtService.isTokenValid(token));
    }
}

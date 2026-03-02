package com.mindtrack.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for generating and validating JWT tokens.
 */
@Service
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${mindtrack.auth.jwt-secret:default-dev-secret-key-change-in-prod-256bit!}")
            String secret,
            @Value("${mindtrack.auth.jwt-expiration-ms:86400000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given user with patient/therapist role flags.
     */
    public String generateToken(Long userId, String email, String role,
                                boolean isPatient, boolean isTherapist) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .claim("isPatient", isPatient)
                .claim("isTherapist", isTherapist)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a JWT token for the given user (defaults: isPatient=true, isTherapist=false).
     */
    public String generateToken(Long userId, String email, String role) {
        return generateToken(userId, email, role, true, false);
    }

    /**
     * Extracts the user ID from a valid JWT token.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts the email from a valid JWT token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extracts the role from a valid JWT token.
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extracts the isPatient flag from a valid JWT token.
     */
    public Boolean getIsPatientFromToken(String token) {
        return parseClaims(token).get("isPatient", Boolean.class);
    }

    /**
     * Extracts the isTherapist flag from a valid JWT token.
     */
    public Boolean getIsTherapistFromToken(String token) {
        return parseClaims(token).get("isTherapist", Boolean.class);
    }

    /**
     * Validates a JWT token.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            LOG.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

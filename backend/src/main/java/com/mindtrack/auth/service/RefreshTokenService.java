package com.mindtrack.auth.service;

import com.mindtrack.auth.model.RefreshToken;
import com.mindtrack.auth.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manages single-use, hashed refresh tokens with 30-day TTL.
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long TTL_DAYS = 30;
    private final SecureRandom secureRandom = new SecureRandom();
    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    /**
     * Creates a new refresh token for the user and returns the raw (unhashed) token value.
     */
    @Transactional
    public String createRefreshToken(Long userId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256Hex(raw);
        repo.save(new RefreshToken(userId, hash, Instant.now().plus(TTL_DAYS, ChronoUnit.DAYS)));
        return raw;
    }

    /**
     * Validates the raw refresh token and returns the associated userId.
     * Marks the token as used (single-use rotation).
     * Throws 401 if the token is invalid, used, or expired.
     */
    @Transactional
    public Long rotateRefreshToken(String rawToken) {
        String hash = sha256Hex(rawToken);
        RefreshToken stored = repo.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isUsed() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Refresh token expired or already used");
        }

        stored.setUsed(true);
        repo.save(stored);
        return stored.getUserId();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

package com.mindtrack.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Stores hashed, single-use refresh tokens for JWT rotation.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SHA-256 hex digest of the raw token value. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    public RefreshToken() {
    }

    public RefreshToken(Long userId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the primary key.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the user ID that owns this refresh token.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Returns the SHA-256 hex digest of the raw token value.
     */
    public String getTokenHash() {
        return tokenHash;
    }

    /**
     * Returns the expiry instant for this token.
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Returns true if this token has already been used (single-use rotation).
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Marks this token as used.
     */
    public void setUsed(boolean used) {
        this.used = used;
    }
}

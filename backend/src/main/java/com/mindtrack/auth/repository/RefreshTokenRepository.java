package com.mindtrack.auth.repository;

import com.mindtrack.auth.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for refresh token persistence operations.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its SHA-256 hash.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Deletes all expired and already-used refresh tokens.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < CURRENT_TIMESTAMP OR t.used = true")
    void deleteExpiredAndUsed();
}

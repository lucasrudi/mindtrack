package com.mindtrack.therapist.repository;

import com.mindtrack.therapist.model.InviteToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for invite token operations.
 */
public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {

    Optional<InviteToken> findByToken(String token);

    /**
     * Deletes all invite tokens that have expired before the given timestamp.
     *
     * @param now the cutoff timestamp; tokens with expiresAt before this value are removed
     */
    @Modifying
    @Query("DELETE FROM InviteToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") java.time.LocalDateTime now);
}

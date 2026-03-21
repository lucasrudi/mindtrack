package com.mindtrack.therapist.repository;

import com.mindtrack.therapist.model.InviteToken;
import java.time.LocalDateTime;
import java.util.List;
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

    Optional<InviteToken> findByInitiatorIdAndRecipientIdAndUsedAtIsNullAndExpiresAtAfter(
            Long initiatorId, Long recipientId, LocalDateTime now);

    /**
     * Returns all expired tokens that have not been used yet.
     * Used to identify stale PENDING therapist-patient relationships before cleanup.
     *
     * @param now the cutoff timestamp; tokens with expiresAt before this value are returned
     */
    List<InviteToken> findByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime now);

    /**
     * Deletes all invite tokens that have expired before the given timestamp.
     *
     * @param now the cutoff timestamp; tokens with expiresAt before this value are removed
     */
    @Modifying
    @Query("DELETE FROM InviteToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") java.time.LocalDateTime now);
}

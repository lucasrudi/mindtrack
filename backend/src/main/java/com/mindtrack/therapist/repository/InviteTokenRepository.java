package com.mindtrack.therapist.repository;

import com.mindtrack.therapist.model.InviteToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for invite token operations.
 */
public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {

    Optional<InviteToken> findByToken(String token);
}

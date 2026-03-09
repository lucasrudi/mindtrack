package com.mindtrack.auth.repository;

import com.mindtrack.auth.model.TherapistRegistrationToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for therapist registration tokens.
 */
public interface TherapistRegistrationTokenRepository
        extends JpaRepository<TherapistRegistrationToken, Long> {

    Optional<TherapistRegistrationToken> findByToken(String token);

    List<TherapistRegistrationToken> findAllByOrderByCreatedAtDesc();
}

package com.mindtrack.auth.repository;

import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for user persistence operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    List<User> findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(LocalDateTime cutoff);
}

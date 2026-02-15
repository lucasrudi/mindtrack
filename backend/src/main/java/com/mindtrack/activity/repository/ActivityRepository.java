package com.mindtrack.activity.repository;

import com.mindtrack.activity.model.Activity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for activity persistence operations.
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Activity> findByUserIdAndActiveOrderByCreatedAtDesc(Long userId, boolean active);

    Optional<Activity> findByIdAndUserId(Long id, Long userId);
}

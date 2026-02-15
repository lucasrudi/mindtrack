package com.mindtrack.goals.repository;

import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for goal persistence operations.
 */
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);
}

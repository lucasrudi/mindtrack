package com.mindtrack.goals.repository;

import com.mindtrack.goals.model.Milestone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for milestone persistence operations.
 */
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByGoalIdOrderByCreatedAtAsc(Long goalId);

    Optional<Milestone> findByIdAndGoalId(Long id, Long goalId);
}

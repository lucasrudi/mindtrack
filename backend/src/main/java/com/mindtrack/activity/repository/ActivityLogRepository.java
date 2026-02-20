package com.mindtrack.activity.repository;

import com.mindtrack.activity.model.ActivityLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for activity log persistence operations.
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByActivityIdOrderByLogDateDesc(Long activityId);

    List<ActivityLog> findByActivityIdAndLogDate(Long activityId, LocalDate logDate);

    List<ActivityLog> findByActivity_UserIdAndLogDateOrderByActivity_NameAsc(
            Long userId, LocalDate logDate);

    Optional<ActivityLog> findByIdAndActivity_UserId(Long id, Long userId);

    List<ActivityLog> findByActivity_UserIdAndLogDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate);
}

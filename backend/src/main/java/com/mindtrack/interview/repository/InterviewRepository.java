package com.mindtrack.interview.repository;

import com.mindtrack.interview.model.Interview;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for interview persistence operations.
 */
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUserIdOrderByInterviewDateDesc(Long userId);

    List<Interview> findByUserIdAndInterviewDateBetweenOrderByInterviewDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Interview> findByIdAndUserId(Long id, Long userId);
}

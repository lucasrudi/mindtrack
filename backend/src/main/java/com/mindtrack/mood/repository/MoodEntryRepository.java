package com.mindtrack.mood.repository;

import com.mindtrack.mood.model.MoodEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for mood entry persistence operations.
 */
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    /**
     * Returns all mood entries for the given user, ordered by creation time descending.
     */
    List<MoodEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
}

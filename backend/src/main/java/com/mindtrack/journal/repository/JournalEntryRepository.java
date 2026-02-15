package com.mindtrack.journal.repository;

import com.mindtrack.journal.model.JournalEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for journal entry persistence operations.
 */
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByUserIdOrderByEntryDateDesc(Long userId);

    List<JournalEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    Optional<JournalEntry> findByIdAndUserId(Long id, Long userId);

    List<JournalEntry> findByUserIdAndSharedWithTherapistOrderByEntryDateDesc(
            Long userId, boolean sharedWithTherapist);
}

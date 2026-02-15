package com.mindtrack.journal.service;

import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.model.JournalEntry;
import com.mindtrack.journal.repository.JournalEntryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for journal entry CRUD and sharing operations.
 */
@Service
public class JournalService {

    private static final Logger LOG = LoggerFactory.getLogger(JournalService.class);

    private final JournalEntryRepository journalEntryRepository;
    private final JournalMapper journalMapper;

    public JournalService(JournalEntryRepository journalEntryRepository,
                          JournalMapper journalMapper) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalMapper = journalMapper;
    }

    /**
     * Creates a new journal entry for the given user.
     */
    @Transactional
    public JournalEntryResponse create(Long userId, JournalEntryRequest request) {
        JournalEntry entry = new JournalEntry();
        entry.setUserId(userId);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        journalMapper.applyRequest(request, entry);

        JournalEntry saved = journalEntryRepository.save(entry);
        LOG.info("Created journal entry {} for user {}", saved.getId(), userId);
        return journalMapper.toResponse(saved);
    }

    /**
     * Lists all journal entries for the given user, ordered by date descending.
     */
    public List<JournalEntryResponse> listByUser(Long userId) {
        return journalEntryRepository.findByUserIdOrderByEntryDateDesc(userId).stream()
                .map(journalMapper::toResponse)
                .toList();
    }

    /**
     * Lists journal entries for the given user within a date range.
     */
    public List<JournalEntryResponse> listByUserAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository
                .findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(userId, startDate, endDate)
                .stream()
                .map(journalMapper::toResponse)
                .toList();
    }

    /**
     * Gets a single journal entry by ID, only if it belongs to the given user.
     */
    public JournalEntryResponse getByIdAndUser(Long entryId, Long userId) {
        return journalEntryRepository.findByIdAndUserId(entryId, userId)
                .map(journalMapper::toResponse)
                .orElse(null);
    }

    /**
     * Updates an existing journal entry.
     */
    @Transactional
    public JournalEntryResponse update(Long entryId, Long userId, JournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserId(entryId, userId)
                .orElse(null);
        if (entry == null) {
            return null;
        }

        journalMapper.applyRequest(request, entry);
        entry.setUpdatedAt(LocalDateTime.now());
        JournalEntry saved = journalEntryRepository.save(entry);
        LOG.info("Updated journal entry {} for user {}", saved.getId(), userId);
        return journalMapper.toResponse(saved);
    }

    /**
     * Deletes a journal entry if it belongs to the given user.
     */
    @Transactional
    public boolean delete(Long entryId, Long userId) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserId(entryId, userId)
                .orElse(null);
        if (entry == null) {
            return false;
        }

        journalEntryRepository.delete(entry);
        LOG.info("Deleted journal entry {} for user {}", entryId, userId);
        return true;
    }

    /**
     * Toggles the shared-with-therapist status of a journal entry.
     */
    @Transactional
    public JournalEntryResponse toggleSharing(Long entryId, Long userId) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserId(entryId, userId)
                .orElse(null);
        if (entry == null) {
            return null;
        }

        entry.setSharedWithTherapist(!entry.isSharedWithTherapist());
        entry.setUpdatedAt(LocalDateTime.now());
        JournalEntry saved = journalEntryRepository.save(entry);
        LOG.info("Toggled sharing for journal entry {} shared={} user {}",
                saved.getId(), saved.isSharedWithTherapist(), userId);
        return journalMapper.toResponse(saved);
    }
}

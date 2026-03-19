package com.mindtrack.mood.service;

import com.mindtrack.mood.dto.MoodEntryRequest;
import com.mindtrack.mood.dto.MoodEntryResponse;
import com.mindtrack.mood.model.MoodEntry;
import com.mindtrack.mood.repository.MoodEntryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for mood entry creation and retrieval.
 */
@Service
public class MoodEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodEntryService.class);

    private final MoodEntryRepository moodEntryRepository;

    /**
     * Constructs the service with the given repository.
     */
    public MoodEntryService(MoodEntryRepository moodEntryRepository) {
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * Creates a new mood entry for the given user.
     */
    @Transactional
    public MoodEntryResponse createEntry(Long userId, MoodEntryRequest request) {
        MoodEntry entry = new MoodEntry();
        entry.setUserId(userId);
        entry.setMoodRating(request.getMoodRating());
        entry.setNotes(request.getNotes());
        entry.setCreatedAt(LocalDateTime.now());

        MoodEntry saved = moodEntryRepository.save(entry);
        LOG.info("Created mood entry {} for user {}", saved.getId(), userId);
        return toResponse(saved);
    }

    /**
     * Returns all mood entries for the given user, ordered by creation time descending.
     */
    public List<MoodEntryResponse> getUserEntries(Long userId) {
        return moodEntryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private MoodEntryResponse toResponse(MoodEntry entry) {
        MoodEntryResponse response = new MoodEntryResponse();
        response.setId(entry.getId());
        response.setMoodRating(entry.getMoodRating());
        response.setNotes(entry.getNotes());
        response.setCreatedAt(entry.getCreatedAt());
        return response;
    }
}

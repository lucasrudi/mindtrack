package com.mindtrack.mood.service;

import com.mindtrack.mood.dto.MoodEntryRequest;
import com.mindtrack.mood.dto.MoodEntryResponse;
import com.mindtrack.mood.model.MoodEntry;
import com.mindtrack.mood.repository.MoodEntryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodEntryServiceTest {

    @Mock
    private MoodEntryRepository moodEntryRepository;

    private MoodEntryService moodEntryService;

    @BeforeEach
    void setUp() {
        moodEntryService = new MoodEntryService(moodEntryRepository);
    }

    @Test
    void shouldCreateEntry() {
        MoodEntryRequest request = new MoodEntryRequest();
        request.setMoodRating(7);
        request.setNotes("Feeling good");

        when(moodEntryRepository.save(any(MoodEntry.class))).thenAnswer(invocation -> {
            MoodEntry saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        MoodEntryResponse result = moodEntryService.createEntry(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(7, result.getMoodRating());
        assertEquals("Feeling good", result.getNotes());
        assertNotNull(result.getCreatedAt());

        ArgumentCaptor<MoodEntry> captor = ArgumentCaptor.forClass(MoodEntry.class);
        verify(moodEntryRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(7, captor.getValue().getMoodRating());
    }

    @Test
    void shouldCreateEntryWithNullNotes() {
        MoodEntryRequest request = new MoodEntryRequest();
        request.setMoodRating(5);
        request.setNotes(null);

        when(moodEntryRepository.save(any(MoodEntry.class))).thenAnswer(invocation -> {
            MoodEntry saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        MoodEntryResponse result = moodEntryService.createEntry(1L, request);

        assertNotNull(result);
        assertEquals(5, result.getMoodRating());
        assertNull(result.getNotes());
    }

    @Test
    void shouldReturnEntriesForUser() {
        MoodEntry entry1 = createEntry(1L, 8, "Great day");
        MoodEntry entry2 = createEntry(2L, 3, "Tough day");
        when(moodEntryRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(entry1, entry2));

        List<MoodEntryResponse> results = moodEntryService.getUserEntries(1L);

        assertEquals(2, results.size());
        assertEquals(8, results.get(0).getMoodRating());
        assertEquals(3, results.get(1).getMoodRating());
    }

    @Test
    void shouldReturnEmptyListForUserWithNoEntries() {
        when(moodEntryRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<MoodEntryResponse> results = moodEntryService.getUserEntries(1L);

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    private MoodEntry createEntry(Long id, int rating, String notes) {
        MoodEntry entry = new MoodEntry();
        entry.setId(id);
        entry.setUserId(1L);
        entry.setMoodRating(rating);
        entry.setNotes(notes);
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }
}

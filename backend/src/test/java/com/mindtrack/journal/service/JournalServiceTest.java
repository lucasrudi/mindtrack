package com.mindtrack.journal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.model.JournalEntry;
import com.mindtrack.journal.repository.JournalEntryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private JournalService journalService;

    @BeforeEach
    void setUp() {
        JournalMapper mapper = new JournalMapper(new ObjectMapper());
        journalService = new JournalService(journalEntryRepository, mapper);
    }

    @Test
    void shouldCreateEntry() {
        JournalEntryRequest request = createRequest();
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> {
            JournalEntry saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        JournalEntryResponse result = journalService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Good day", result.getTitle());
        assertEquals("Today was productive", result.getContent());
        assertEquals(7, result.getMood());
        assertEquals(List.of("gratitude", "work"), result.getTags());

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void shouldListAllEntriesByUser() {
        JournalEntry e1 = createEntry(1L);
        JournalEntry e2 = createEntry(2L);
        when(journalEntryRepository.findByUserIdOrderByEntryDateDesc(1L))
                .thenReturn(List.of(e1, e2));

        List<JournalEntryResponse> results = journalService.listByUser(1L);

        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnEmptyListForUserWithNoEntries() {
        when(journalEntryRepository.findByUserIdOrderByEntryDateDesc(1L))
                .thenReturn(List.of());

        List<JournalEntryResponse> results = journalService.listByUser(1L);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldListEntriesByDateRange() {
        JournalEntry e1 = createEntry(1L);
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        when(journalEntryRepository
                .findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(1L, start, end))
                .thenReturn(List.of(e1));

        List<JournalEntryResponse> results =
                journalService.listByUserAndDateRange(1L, start, end);

        assertEquals(1, results.size());
    }

    @Test
    void shouldGetEntryByIdAndUser() {
        JournalEntry entry = createEntry(1L);
        when(journalEntryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(entry));

        JournalEntryResponse result = journalService.getByIdAndUser(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Good day", result.getTitle());
    }

    @Test
    void shouldReturnNullWhenEntryNotFound() {
        when(journalEntryRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(journalService.getByIdAndUser(999L, 1L));
    }

    @Test
    void shouldUpdateEntry() {
        JournalEntry existing = createEntry(1L);
        JournalEntryRequest request = createRequest();
        request.setTitle("Updated title");
        request.setContent("Updated content");

        when(journalEntryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(existing);

        JournalEntryResponse result = journalService.update(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated content", result.getContent());
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistent() {
        when(journalEntryRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(journalService.update(999L, 1L, createRequest()));
        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteEntry() {
        JournalEntry entry = createEntry(1L);
        when(journalEntryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(entry));

        assertTrue(journalService.delete(1L, 1L));
        verify(journalEntryRepository).delete(entry);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistent() {
        when(journalEntryRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(journalService.delete(999L, 1L));
        verify(journalEntryRepository, never()).delete(any());
    }

    @Test
    void shouldToggleSharing() {
        JournalEntry entry = createEntry(1L);
        assertFalse(entry.isSharedWithTherapist());

        when(journalEntryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(entry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(entry);

        JournalEntryResponse result = journalService.toggleSharing(1L, 1L);

        assertNotNull(result);
        assertTrue(result.isSharedWithTherapist());
    }

    @Test
    void shouldReturnNullWhenTogglingSharingOnNonExistent() {
        when(journalEntryRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(journalService.toggleSharing(999L, 1L));
        verify(journalEntryRepository, never()).save(any());
    }

    private JournalEntryRequest createRequest() {
        JournalEntryRequest request = new JournalEntryRequest();
        request.setEntryDate(LocalDate.of(2025, 1, 15));
        request.setTitle("Good day");
        request.setContent("Today was productive");
        request.setMood(7);
        request.setTags(List.of("gratitude", "work"));
        request.setSharedWithTherapist(false);
        return request;
    }

    private JournalEntry createEntry(Long id) {
        JournalEntry entry = new JournalEntry();
        entry.setId(id);
        entry.setUserId(1L);
        entry.setEntryDate(LocalDate.of(2025, 1, 15));
        entry.setTitle("Good day");
        entry.setContent("Today was productive");
        entry.setMood(7);
        entry.setTags("[\"gratitude\",\"work\"]");
        entry.setSharedWithTherapist(false);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        return entry;
    }
}

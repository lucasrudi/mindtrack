package com.mindtrack.journal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.model.JournalEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JournalMapperTest {

    private JournalMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JournalMapper(new ObjectMapper());
    }

    @Test
    void shouldMapEntryToResponse() {
        JournalEntry entry = createEntry(1L);

        JournalEntryResponse response = mapper.toResponse(entry);

        assertEquals(1L, response.getId());
        assertEquals(LocalDate.of(2025, 1, 15), response.getEntryDate());
        assertEquals("Good day", response.getTitle());
        assertEquals("Today was productive", response.getContent());
        assertEquals(7, response.getMood());
        assertEquals(List.of("gratitude", "work"), response.getTags());
        assertFalse(response.isSharedWithTherapist());
    }

    @Test
    void shouldApplyRequestToEntry() {
        JournalEntryRequest request = new JournalEntryRequest();
        request.setEntryDate(LocalDate.of(2025, 2, 1));
        request.setTitle("Tough day");
        request.setContent("Had some challenges");
        request.setMood(4);
        request.setTags(List.of("anxiety", "therapy"));
        request.setSharedWithTherapist(true);

        JournalEntry entry = new JournalEntry();
        mapper.applyRequest(request, entry);

        assertEquals(LocalDate.of(2025, 2, 1), entry.getEntryDate());
        assertEquals("Tough day", entry.getTitle());
        assertEquals("Had some challenges", entry.getContent());
        assertEquals(4, entry.getMood());
        assertEquals("[\"anxiety\",\"therapy\"]", entry.getTags());
        assertTrue(entry.isSharedWithTherapist());
    }

    @Test
    void shouldParseNullTags() {
        List<String> result = mapper.parseTags(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParseBlankTags() {
        List<String> result = mapper.parseTags("  ");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParseInvalidJsonTags() {
        List<String> result = mapper.parseTags("not json");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParseValidJsonTags() {
        List<String> result = mapper.parseTags("[\"mood\",\"sleep\"]");
        assertEquals(2, result.size());
        assertEquals("mood", result.get(0));
        assertEquals("sleep", result.get(1));
    }

    @Test
    void shouldSerializeNullTags() {
        assertNull(mapper.serializeTags(null));
    }

    @Test
    void shouldSerializeEmptyTags() {
        assertNull(mapper.serializeTags(Collections.emptyList()));
    }

    @Test
    void shouldSerializeValidTags() {
        String result = mapper.serializeTags(List.of("gratitude", "work"));
        assertEquals("[\"gratitude\",\"work\"]", result);
    }

    @Test
    void shouldMapEntryWithNullTags() {
        JournalEntry entry = createEntry(1L);
        entry.setTags(null);

        JournalEntryResponse response = mapper.toResponse(entry);

        assertTrue(response.getTags().isEmpty());
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

package com.mindtrack.journal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.model.JournalEntry;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Maps between JournalEntry entities and DTOs.
 */
@Component
public class JournalMapper {

    private static final Logger LOG = LoggerFactory.getLogger(JournalMapper.class);
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper;

    public JournalMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a JournalEntry entity to a response DTO.
     */
    public JournalEntryResponse toResponse(JournalEntry entry) {
        JournalEntryResponse response = new JournalEntryResponse();
        response.setId(entry.getId());
        response.setEntryDate(entry.getEntryDate());
        response.setTitle(entry.getTitle());
        response.setContent(entry.getContent());
        response.setMood(entry.getMood());
        response.setTags(parseTags(entry.getTags()));
        response.setSharedWithTherapist(entry.isSharedWithTherapist());
        response.setCreatedAt(entry.getCreatedAt());
        response.setUpdatedAt(entry.getUpdatedAt());
        return response;
    }

    /**
     * Applies request DTO fields to a JournalEntry entity.
     */
    public void applyRequest(JournalEntryRequest request, JournalEntry entry) {
        entry.setEntryDate(request.getEntryDate());
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setMood(request.getMood());
        entry.setTags(serializeTags(request.getTags()));
        entry.setSharedWithTherapist(request.isSharedWithTherapist());
    }

    /**
     * Parses a JSON string into a list of tag strings.
     */
    List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagsJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to parse tags JSON: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Serializes a list of tag strings to JSON.
     */
    String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to serialize tags: {}", ex.getMessage());
            return null;
        }
    }
}

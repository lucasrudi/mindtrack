package com.mindtrack.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.model.Interview;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Maps between Interview entities and DTOs.
 */
@Component
public class InterviewMapper {

    private static final Logger LOG = LoggerFactory.getLogger(InterviewMapper.class);
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper;

    public InterviewMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts an Interview entity to a response DTO.
     */
    public InterviewResponse toResponse(Interview interview) {
        InterviewResponse response = new InterviewResponse();
        response.setId(interview.getId());
        response.setInterviewDate(interview.getInterviewDate());
        response.setMoodBefore(interview.getMoodBefore());
        response.setMoodAfter(interview.getMoodAfter());
        response.setTopics(parseTopics(interview.getTopics()));
        response.setMedicationChanges(interview.getMedicationChanges());
        response.setRecommendations(interview.getRecommendations());
        response.setNotes(interview.getNotes());
        response.setHasAudio(interview.getAudioS3Key() != null);
        response.setTranscriptionText(interview.getTranscriptionText());
        response.setAudioExpiresAt(interview.getAudioExpiresAt());
        response.setCreatedAt(interview.getCreatedAt());
        response.setUpdatedAt(interview.getUpdatedAt());
        return response;
    }

    /**
     * Applies request DTO fields to an Interview entity.
     */
    public void applyRequest(InterviewRequest request, Interview interview) {
        interview.setInterviewDate(request.getInterviewDate());
        interview.setMoodBefore(request.getMoodBefore());
        interview.setMoodAfter(request.getMoodAfter());
        interview.setTopics(serializeTopics(request.getTopics()));
        interview.setMedicationChanges(request.getMedicationChanges());
        interview.setRecommendations(request.getRecommendations());
        interview.setNotes(request.getNotes());
    }

    private List<String> parseTopics(String topicsJson) {
        if (topicsJson == null || topicsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(topicsJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to parse topics JSON: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String serializeTopics(List<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(topics);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to serialize topics: {}", ex.getMessage());
            return null;
        }
    }
}

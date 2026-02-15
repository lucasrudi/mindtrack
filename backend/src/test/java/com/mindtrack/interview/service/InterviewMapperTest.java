package com.mindtrack.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.model.Interview;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterviewMapperTest {

    private InterviewMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InterviewMapper(new ObjectMapper());
    }

    @Test
    void shouldMapEntityToResponse() {
        Interview interview = createInterview();

        InterviewResponse response = mapper.toResponse(interview);

        assertEquals(1L, response.getId());
        assertEquals(LocalDate.of(2025, 1, 15), response.getInterviewDate());
        assertEquals(5, response.getMoodBefore());
        assertEquals(7, response.getMoodAfter());
        assertEquals(List.of("anxiety", "sleep"), response.getTopics());
        assertEquals("Increased dosage", response.getMedicationChanges());
        assertEquals("Try meditation", response.getRecommendations());
        assertEquals("Good session", response.getNotes());
        assertFalse(response.isHasAudio());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void shouldSetHasAudioWhenS3KeyPresent() {
        Interview interview = createInterview();
        interview.setAudioS3Key("interviews/1/audio.mp3");

        InterviewResponse response = mapper.toResponse(interview);

        assertTrue(response.isHasAudio());
    }

    @Test
    void shouldHandleNullTopicsJson() {
        Interview interview = createInterview();
        interview.setTopics(null);

        InterviewResponse response = mapper.toResponse(interview);

        assertNotNull(response.getTopics());
        assertTrue(response.getTopics().isEmpty());
    }

    @Test
    void shouldHandleBlankTopicsJson() {
        Interview interview = createInterview();
        interview.setTopics("   ");

        InterviewResponse response = mapper.toResponse(interview);

        assertNotNull(response.getTopics());
        assertTrue(response.getTopics().isEmpty());
    }

    @Test
    void shouldHandleInvalidTopicsJson() {
        Interview interview = createInterview();
        interview.setTopics("not-valid-json");

        InterviewResponse response = mapper.toResponse(interview);

        assertNotNull(response.getTopics());
        assertTrue(response.getTopics().isEmpty());
    }

    @Test
    void shouldApplyRequestToEntity() {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 2, 20));
        request.setMoodBefore(3);
        request.setMoodAfter(8);
        request.setTopics(List.of("depression", "work stress"));
        request.setMedicationChanges("No changes");
        request.setRecommendations("Exercise daily");
        request.setNotes("Follow up in 2 weeks");

        Interview interview = new Interview();
        mapper.applyRequest(request, interview);

        assertEquals(LocalDate.of(2025, 2, 20), interview.getInterviewDate());
        assertEquals(3, interview.getMoodBefore());
        assertEquals(8, interview.getMoodAfter());
        assertEquals("[\"depression\",\"work stress\"]", interview.getTopics());
        assertEquals("No changes", interview.getMedicationChanges());
        assertEquals("Exercise daily", interview.getRecommendations());
        assertEquals("Follow up in 2 weeks", interview.getNotes());
    }

    @Test
    void shouldSerializeNullTopicsAsNull() {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 1, 1));
        request.setTopics(null);

        Interview interview = new Interview();
        mapper.applyRequest(request, interview);

        assertNull(interview.getTopics());
    }

    @Test
    void shouldSerializeEmptyTopicsAsNull() {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 1, 1));
        request.setTopics(Collections.emptyList());

        Interview interview = new Interview();
        mapper.applyRequest(request, interview);

        assertNull(interview.getTopics());
    }

    private Interview createInterview() {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setUserId(1L);
        interview.setInterviewDate(LocalDate.of(2025, 1, 15));
        interview.setMoodBefore(5);
        interview.setMoodAfter(7);
        interview.setTopics("[\"anxiety\",\"sleep\"]");
        interview.setMedicationChanges("Increased dosage");
        interview.setRecommendations("Try meditation");
        interview.setNotes("Good session");
        interview.setCreatedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());
        return interview;
    }
}

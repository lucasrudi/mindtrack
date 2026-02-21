package com.mindtrack.therapist.service;

import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TherapistMapperTest {

    private TherapistMapper therapistMapper;

    @BeforeEach
    void setUp() {
        therapistMapper = new TherapistMapper();
    }

    @Test
    void shouldMapAllFieldsToPatientSummary() {
        User user = new User();
        user.setId(1L);
        user.setName("John Patient");
        user.setEmail("john@example.com");

        LocalDateTime lastInterview = LocalDateTime.of(2025, 1, 15, 10, 30);

        PatientSummaryResponse result = therapistMapper.toPatientSummary(
                user, 5, 3, 8, lastInterview);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Patient", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(5, result.getInterviewCount());
        assertEquals(3, result.getActiveGoalCount());
        assertEquals(8, result.getActivityCount());
        assertEquals(lastInterview, result.getLastInterviewDate());
    }

    @Test
    void shouldHandleNullLastInterviewDate() {
        User user = new User();
        user.setId(2L);
        user.setName("Jane Patient");
        user.setEmail("jane@example.com");

        PatientSummaryResponse result = therapistMapper.toPatientSummary(
                user, 0, 0, 0, null);

        assertNotNull(result);
        assertEquals(0, result.getInterviewCount());
        assertNull(result.getLastInterviewDate());
    }
}

package com.mindtrack.therapist.service;

import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Maps patient data to therapist view DTOs.
 */
@Component
public class TherapistMapper {

    /**
     * Builds a patient summary from user data and aggregated counts.
     */
    public PatientSummaryResponse toPatientSummary(User user, int interviewCount,
                                                    int activeGoalCount, int activityCount,
                                                    LocalDateTime lastInterviewDate,
                                                    String calendarColor,
                                                    String status) {
        PatientSummaryResponse response = new PatientSummaryResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCalendarColor(calendarColor);
        response.setInterviewCount(interviewCount);
        response.setActiveGoalCount(activeGoalCount);
        response.setActivityCount(activityCount);
        response.setLastInterviewDate(lastInterviewDate);
        response.setStatus(status);
        return response;
    }
}

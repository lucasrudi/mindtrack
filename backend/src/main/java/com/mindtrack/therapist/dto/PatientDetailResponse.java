package com.mindtrack.therapist.dto;

import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.journal.dto.JournalEntryResponse;
import java.util.List;

/**
 * Detailed DTO wrapping all read-only patient data for the therapist view.
 */
public class PatientDetailResponse {

    private Long patientId;
    private String patientName;
    private String patientEmail;
    private List<InterviewResponse> interviews;
    private List<ActivityResponse> activities;
    private List<GoalResponse> goals;
    private List<JournalEntryResponse> sharedJournalEntries;

    public PatientDetailResponse() {
    }

    public PatientDetailResponse(Long patientId, String patientName, String patientEmail,
                                  List<InterviewResponse> interviews,
                                  List<ActivityResponse> activities,
                                  List<GoalResponse> goals,
                                  List<JournalEntryResponse> sharedJournalEntries) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.interviews = interviews;
        this.activities = activities;
        this.goals = goals;
        this.sharedJournalEntries = sharedJournalEntries;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public List<InterviewResponse> getInterviews() {
        return interviews;
    }

    public void setInterviews(List<InterviewResponse> interviews) {
        this.interviews = interviews;
    }

    public List<ActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
    }

    public List<GoalResponse> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalResponse> goals) {
        this.goals = goals;
    }

    public List<JournalEntryResponse> getSharedJournalEntries() {
        return sharedJournalEntries;
    }

    public void setSharedJournalEntries(List<JournalEntryResponse> sharedJournalEntries) {
        this.sharedJournalEntries = sharedJournalEntries;
    }
}

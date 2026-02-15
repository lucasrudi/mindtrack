package com.mindtrack.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating or updating an interview.
 */
public class InterviewRequest {

    @NotNull(message = "Interview date is required")
    private LocalDate interviewDate;

    @Min(value = 1, message = "Mood must be between 1 and 10")
    @Max(value = 10, message = "Mood must be between 1 and 10")
    private Integer moodBefore;

    @Min(value = 1, message = "Mood must be between 1 and 10")
    @Max(value = 10, message = "Mood must be between 1 and 10")
    private Integer moodAfter;

    private List<String> topics;

    private String medicationChanges;

    private String recommendations;

    private String notes;

    public InterviewRequest() {
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public Integer getMoodBefore() {
        return moodBefore;
    }

    public void setMoodBefore(Integer moodBefore) {
        this.moodBefore = moodBefore;
    }

    public Integer getMoodAfter() {
        return moodAfter;
    }

    public void setMoodAfter(Integer moodAfter) {
        this.moodAfter = moodAfter;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getMedicationChanges() {
        return medicationChanges;
    }

    public void setMedicationChanges(String medicationChanges) {
        this.medicationChanges = medicationChanges;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

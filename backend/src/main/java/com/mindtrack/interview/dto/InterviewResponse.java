package com.mindtrack.interview.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for interview data.
 */
public class InterviewResponse {

    private Long id;
    private LocalDate interviewDate;
    private Integer moodBefore;
    private Integer moodAfter;
    private List<String> topics;
    private String medicationChanges;
    private String recommendations;
    private String notes;
    private boolean hasAudio;
    private String transcriptionText;
    private LocalDateTime audioExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public void setTranscriptionText(String transcriptionText) {
        this.transcriptionText = transcriptionText;
    }

    public LocalDateTime getAudioExpiresAt() {
        return audioExpiresAt;
    }

    public void setAudioExpiresAt(LocalDateTime audioExpiresAt) {
        this.audioExpiresAt = audioExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

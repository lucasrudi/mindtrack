package com.mindtrack.interview.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity representing a psychiatrist interview session.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "interview_date", nullable = false)
    private LocalDate interviewDate;

    @Column(name = "mood_before")
    private Integer moodBefore;

    @Column(name = "mood_after")
    private Integer moodAfter;

    @Column(columnDefinition = "JSON")
    private String topics;

    @Column(name = "medication_changes", columnDefinition = "TEXT")
    private String medicationChanges;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "transcription_text", columnDefinition = "LONGTEXT")
    private String transcriptionText;

    @Column(name = "audio_s3_key", length = 500)
    private String audioS3Key;

    @Column(name = "audio_expires_at")
    private LocalDateTime audioExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcription_status")
    private TranscriptionStatus transcriptionStatus;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Interview() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
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

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public void setTranscriptionText(String transcriptionText) {
        this.transcriptionText = transcriptionText;
    }

    public String getAudioS3Key() {
        return audioS3Key;
    }

    public void setAudioS3Key(String audioS3Key) {
        this.audioS3Key = audioS3Key;
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

    public TranscriptionStatus getTranscriptionStatus() {
        return transcriptionStatus;
    }

    public void setTranscriptionStatus(TranscriptionStatus transcriptionStatus) {
        this.transcriptionStatus = transcriptionStatus;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}

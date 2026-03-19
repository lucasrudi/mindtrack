package com.mindtrack.mood.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for mood entry data.
 */
public class MoodEntryResponse {

    private Long id;
    private Integer moodRating;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * Required by Jackson for response serialization/deserialization.
     */
    public MoodEntryResponse() {
    }

    /**
     * Returns the entry ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the entry ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the mood rating.
     */
    public Integer getMoodRating() {
        return moodRating;
    }

    /**
     * Sets the mood rating.
     */
    public void setMoodRating(Integer moodRating) {
        this.moodRating = moodRating;
    }

    /**
     * Returns optional notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets optional notes.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Returns the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

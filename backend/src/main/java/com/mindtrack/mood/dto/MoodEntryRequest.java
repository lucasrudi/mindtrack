package com.mindtrack.mood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a mood entry.
 */
public class MoodEntryRequest {

    @NotNull(message = "Mood rating is required")
    @Min(value = 1, message = "Mood rating must be between 1 and 10")
    @Max(value = 10, message = "Mood rating must be between 1 and 10")
    private Integer moodRating;

    private String notes;

    /**
     * Required by Jackson for request-body deserialization.
     */
    public MoodEntryRequest() {
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
}

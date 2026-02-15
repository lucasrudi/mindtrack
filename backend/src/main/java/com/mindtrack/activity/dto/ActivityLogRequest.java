package com.mindtrack.activity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for logging an activity completion.
 */
public class ActivityLogRequest {

    @NotNull(message = "Log date is required")
    private LocalDate logDate;

    private boolean completed;

    private String notes;

    @Min(value = 1, message = "Mood must be between 1 and 10")
    @Max(value = 10, message = "Mood must be between 1 and 10")
    private Integer moodRating;

    public ActivityLogRequest() {
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getMoodRating() {
        return moodRating;
    }

    public void setMoodRating(Integer moodRating) {
        this.moodRating = moodRating;
    }
}

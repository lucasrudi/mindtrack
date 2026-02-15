package com.mindtrack.goals.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a milestone.
 */
public class MilestoneRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private LocalDate targetDate;

    private String notes;

    public MilestoneRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

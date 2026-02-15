package com.mindtrack.goals.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a goal.
 */
public class GoalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    private LocalDate targetDate;

    public GoalRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}

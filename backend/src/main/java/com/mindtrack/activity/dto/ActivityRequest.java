package com.mindtrack.activity.dto;

import com.mindtrack.activity.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating an activity.
 */
public class ActivityRequest {

    @NotNull(message = "Activity type is required")
    private ActivityType type;

    @NotBlank(message = "Activity name is required")
    private String name;

    private String description;

    private String frequency;

    private Long linkedInterviewId;

    public ActivityRequest() {
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Long getLinkedInterviewId() {
        return linkedInterviewId;
    }

    public void setLinkedInterviewId(Long linkedInterviewId) {
        this.linkedInterviewId = linkedInterviewId;
    }
}

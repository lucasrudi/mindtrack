package com.mindtrack.activity.dto;

import com.mindtrack.activity.model.ActivityType;
import java.time.LocalDateTime;

/**
 * Response DTO for activity data.
 */
public class ActivityResponse {

    private Long id;
    private ActivityType type;
    private String name;
    private String description;
    private String frequency;
    private Long linkedInterviewId;
    private boolean active;
    private LocalDateTime createdAt;

    public ActivityResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

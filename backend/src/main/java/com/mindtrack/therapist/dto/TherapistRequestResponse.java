package com.mindtrack.therapist.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for a patient's incoming therapist connection request.
 */
public class TherapistRequestResponse {

    private Long relationshipId;
    private Long therapistId;
    private String therapistName;
    private String therapistEmail;
    private String status;
    private LocalDateTime createdAt;

    public TherapistRequestResponse() {
        // Default constructor for JSON serialization.
    }

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public void setTherapistName(String therapistName) {
        this.therapistName = therapistName;
    }

    public String getTherapistEmail() {
        return therapistEmail;
    }

    public void setTherapistEmail(String therapistEmail) {
        this.therapistEmail = therapistEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.mindtrack.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request payload for booking an appointment.
 */
public class AppointmentRequest {

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be in the future")
    private LocalDateTime startAt;

    @NotNull(message = "End time is required")
    @FutureOrPresent(message = "End time must be in the future")
    private LocalDateTime endAt;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must be 255 characters or less")
    private String reason;

    @Size(max = 2000, message = "Notes must be 2000 characters or less")
    private String notes;

    public AppointmentRequest() {
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

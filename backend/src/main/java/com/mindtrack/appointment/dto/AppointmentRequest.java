package com.mindtrack.appointment.dto;

import com.mindtrack.appointment.model.RecurrenceType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request payload for booking an appointment.
 */
public class AppointmentRequest {

    private static final int DEFAULT_DURATION_MINUTES = 50;
    private static final int DEFAULT_RECURRENCE_COUNT = 12;
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 480;
    private static final int MIN_RECURRENCE_COUNT = 1;
    private static final int MAX_RECURRENCE_COUNT = 104;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be in the future")
    private LocalDateTime startAt;

    @Min(value = MIN_DURATION, message = "Duration must be at least 1 minute")
    @Max(value = MAX_DURATION, message = "Duration must be at most 480 minutes")
    private int durationMinutes = DEFAULT_DURATION_MINUTES;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must be 255 characters or less")
    private String reason;

    @Size(max = 2000, message = "Notes must be 2000 characters or less")
    private String notes;

    private RecurrenceType recurrence = RecurrenceType.NONE;

    private LocalDate recurrenceEndDate;

    @Min(value = MIN_RECURRENCE_COUNT, message = "Recurrence count must be at least 1")
    @Max(value = MAX_RECURRENCE_COUNT, message = "Recurrence count must be at most 104")
    private Integer recurrenceCount = DEFAULT_RECURRENCE_COUNT;

    public AppointmentRequest() {
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
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

    public RecurrenceType getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(RecurrenceType recurrence) {
        this.recurrence = recurrence;
    }

    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    public Integer getRecurrenceCount() {
        return recurrenceCount;
    }

    public void setRecurrenceCount(Integer recurrenceCount) {
        this.recurrenceCount = recurrenceCount;
    }
}

package com.mindtrack.therapist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for updating a patient's calendar color preference.
 */
public class CalendarColorRequest {

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String calendarColor;

    public CalendarColorRequest() {
        // Default constructor for JSON deserialization.
    }

    public String getCalendarColor() {
        return calendarColor;
    }

    public void setCalendarColor(String calendarColor) {
        this.calendarColor = calendarColor;
    }
}

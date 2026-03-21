package com.mindtrack.therapist.dto;

import java.time.LocalDateTime;

/**
 * Summary DTO for a therapist's patient list.
 */
public class PatientSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String calendarColor;
    private int interviewCount;
    private int activeGoalCount;
    private int activityCount;
    private LocalDateTime lastInterviewDate;
    private String status;

    public PatientSummaryResponse() {
        // Default constructor for JSON serialization.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCalendarColor() {
        return calendarColor;
    }

    public void setCalendarColor(String calendarColor) {
        this.calendarColor = calendarColor;
    }

    public int getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(int interviewCount) {
        this.interviewCount = interviewCount;
    }

    public int getActiveGoalCount() {
        return activeGoalCount;
    }

    public void setActiveGoalCount(int activeGoalCount) {
        this.activeGoalCount = activeGoalCount;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }

    public LocalDateTime getLastInterviewDate() {
        return lastInterviewDate;
    }

    public void setLastInterviewDate(LocalDateTime lastInterviewDate) {
        this.lastInterviewDate = lastInterviewDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

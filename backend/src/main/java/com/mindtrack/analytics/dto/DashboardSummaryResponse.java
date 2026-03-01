package com.mindtrack.analytics.dto;

/**
 * Dashboard summary with key metrics for the user.
 */
public class DashboardSummaryResponse {

    private long totalJournalEntries;
    private Double averageMood;
    private long totalActivitiesLogged;
    private Double activityCompletionRate;
    private long totalGoals;
    private long completedGoals;
    private long activeGoals;
    private long validatedGoals;
    private long pendingValidationGoals;

    public DashboardSummaryResponse() {
    }

    public long getTotalJournalEntries() {
        return totalJournalEntries;
    }

    public void setTotalJournalEntries(long totalJournalEntries) {
        this.totalJournalEntries = totalJournalEntries;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
    }

    public long getTotalActivitiesLogged() {
        return totalActivitiesLogged;
    }

    public void setTotalActivitiesLogged(long totalActivitiesLogged) {
        this.totalActivitiesLogged = totalActivitiesLogged;
    }

    public Double getActivityCompletionRate() {
        return activityCompletionRate;
    }

    public void setActivityCompletionRate(Double activityCompletionRate) {
        this.activityCompletionRate = activityCompletionRate;
    }

    public long getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(long totalGoals) {
        this.totalGoals = totalGoals;
    }

    public long getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(long completedGoals) {
        this.completedGoals = completedGoals;
    }

    public long getActiveGoals() {
        return activeGoals;
    }

    public void setActiveGoals(long activeGoals) {
        this.activeGoals = activeGoals;
    }

    public long getValidatedGoals() {
        return validatedGoals;
    }

    public void setValidatedGoals(long validatedGoals) {
        this.validatedGoals = validatedGoals;
    }

    public long getPendingValidationGoals() {
        return pendingValidationGoals;
    }

    public void setPendingValidationGoals(long pendingValidationGoals) {
        this.pendingValidationGoals = pendingValidationGoals;
    }
}

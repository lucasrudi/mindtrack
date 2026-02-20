package com.mindtrack.analytics.dto;

/**
 * Activity completion statistics by activity type.
 */
public class ActivityStatsResponse {

    private String activityType;
    private long totalLogs;
    private long completedLogs;
    private Double completionRate;

    public ActivityStatsResponse() {
    }

    public ActivityStatsResponse(String activityType, long totalLogs, long completedLogs,
                                 Double completionRate) {
        this.activityType = activityType;
        this.totalLogs = totalLogs;
        this.completedLogs = completedLogs;
        this.completionRate = completionRate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getCompletedLogs() {
        return completedLogs;
    }

    public void setCompletedLogs(long completedLogs) {
        this.completedLogs = completedLogs;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
}

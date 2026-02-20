package com.mindtrack.analytics.dto;

/**
 * Goal count by status for progress visualization.
 */
public class GoalProgressResponse {

    private String status;
    private long count;

    public GoalProgressResponse() {
    }

    public GoalProgressResponse(String status, long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

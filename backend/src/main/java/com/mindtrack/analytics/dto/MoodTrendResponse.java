package com.mindtrack.analytics.dto;

import java.time.LocalDate;

/**
 * Mood trend data point for a specific date.
 */
public class MoodTrendResponse {

    private LocalDate date;
    private Double averageMood;
    private int entryCount;

    public MoodTrendResponse() {
    }

    public MoodTrendResponse(LocalDate date, Double averageMood, int entryCount) {
        this.date = date;
        this.averageMood = averageMood;
        this.entryCount = entryCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }
}

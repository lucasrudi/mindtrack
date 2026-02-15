package com.mindtrack.journal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating or updating a journal entry.
 */
public class JournalEntryRequest {

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    private String title;

    private String content;

    @Min(value = 1, message = "Mood must be between 1 and 10")
    @Max(value = 10, message = "Mood must be between 1 and 10")
    private Integer mood;

    private List<String> tags;

    private boolean sharedWithTherapist;

    public JournalEntryRequest() {
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isSharedWithTherapist() {
        return sharedWithTherapist;
    }

    public void setSharedWithTherapist(boolean sharedWithTherapist) {
        this.sharedWithTherapist = sharedWithTherapist;
    }
}

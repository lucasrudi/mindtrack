package com.mindtrack.journal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for journal entry data.
 */
public class JournalEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private String title;
    private String content;
    private Integer mood;
    private List<String> tags;
    private boolean sharedWithTherapist;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JournalEntryResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

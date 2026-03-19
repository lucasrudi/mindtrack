package com.mindtrack.mood.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity representing a mood entry logged by a user.
 */
@Entity
@Table(name = "mood_entries")
public class MoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mood_rating", nullable = false)
    private Integer moodRating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Required by JPA for entity materialization.
     */
    public MoodEntry() {
    }

    /**
     * Returns the entry ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the entry ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user ID that owns this entry.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user ID that owns this entry.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the mood rating (1-10).
     */
    public Integer getMoodRating() {
        return moodRating;
    }

    /**
     * Sets the mood rating (1-10).
     */
    public void setMoodRating(Integer moodRating) {
        this.moodRating = moodRating;
    }

    /**
     * Returns optional notes for this mood entry.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets optional notes for this mood entry.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Returns the timestamp when the entry was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the entry was created.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.mindtrack.auth.dto;

import java.time.LocalDateTime;

/**
 * DTO returned when an admin creates a therapist registration token.
 */
public class TherapistTokenResponse {

    private Long id;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Long usedBy;
    private LocalDateTime usedAt;

    public TherapistTokenResponse() {
    }

    public TherapistTokenResponse(Long id, String token, LocalDateTime expiresAt,
            LocalDateTime createdAt, Long usedBy, LocalDateTime usedAt) {
        this.id = id;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Long usedBy) {
        this.usedBy = usedBy;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}

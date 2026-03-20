package com.mindtrack.notifications.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for a single notification.
 */
public class NotificationDTO {

    private Long id;
    private String type;
    private String title;
    private String body;
    private boolean read;
    private String link;
    private LocalDateTime createdAt;

    public NotificationDTO() {
        // Required by Jackson for response serialization/deserialization.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

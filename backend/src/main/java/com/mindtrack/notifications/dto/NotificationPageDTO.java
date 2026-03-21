package com.mindtrack.notifications.dto;

import java.util.List;

/**
 * Paginated response DTO for a list of notifications.
 */
public class NotificationPageDTO {

    private List<NotificationDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public NotificationPageDTO() {
        // Required by Jackson for response serialization/deserialization.
    }

    public List<NotificationDTO> getContent() {
        return content;
    }

    public void setContent(List<NotificationDTO> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

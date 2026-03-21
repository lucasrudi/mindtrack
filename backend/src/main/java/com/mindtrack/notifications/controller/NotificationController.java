package com.mindtrack.notifications.controller;

import com.mindtrack.notifications.dto.NotificationDTO;
import com.mindtrack.notifications.dto.NotificationPageDTO;
import com.mindtrack.notifications.service.NotificationService;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for notification retrieval and mark-read operations.
 *
 * <p>All endpoints are user-scoped — only the authenticated user's notifications are accessible.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Returns a paginated list of notifications for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<NotificationPageDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    /**
     * Returns the count of unread notifications for the authenticated user.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Marks a single notification as read.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markRead(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationDTO dto = notificationService.markRead(id, userId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Marks all notifications as read for the authenticated user.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }
}

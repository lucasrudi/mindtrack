package com.mindtrack.notifications.service;

import com.mindtrack.notifications.dto.NotificationDTO;
import com.mindtrack.notifications.dto.NotificationPageDTO;
import com.mindtrack.notifications.model.Notification;
import com.mindtrack.notifications.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for in-app notification CRUD and marking operations.
 */
@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a new in-app notification for the given user.
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, String type, String title,
                                              String body, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setLink(link);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        LOG.info("Created notification id={} for userId={}", saved.getId(), userId);
        return toDTO(saved);
    }

    /**
     * Returns a paginated list of notifications for the given user, newest first.
     */
    public NotificationPageDTO getNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDTO> dtos = page.getContent().stream().map(this::toDTO).toList();

        NotificationPageDTO result = new NotificationPageDTO();
        result.setContent(dtos);
        result.setPage(page.getNumber());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        return result;
    }

    /**
     * Marks a single notification as read, only if it belongs to the given user.
     *
     * @return the updated DTO, or null if the notification was not found
     */
    @Transactional
    public NotificationDTO markRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId).orElse(null);
        if (notification == null) {
            return null;
        }
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        LOG.info("Marked notification id={} read for userId={}", id, userId);
        return toDTO(saved);
    }

    /**
     * Marks all unread notifications as read for the given user.
     */
    @Transactional
    public void markAllRead(Long userId) {
        int updated = notificationRepository.markAllReadByUserId(userId);
        LOG.info("Marked {} notifications read for userId={}", updated, userId);
    }

    /**
     * Returns the count of unread notifications for the given user.
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setRead(notification.isRead());
        dto.setLink(notification.getLink());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}

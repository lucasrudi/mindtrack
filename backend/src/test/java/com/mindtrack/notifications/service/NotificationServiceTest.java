package com.mindtrack.notifications.service;

import com.mindtrack.notifications.dto.NotificationDTO;
import com.mindtrack.notifications.dto.NotificationPageDTO;
import com.mindtrack.notifications.model.Notification;
import com.mindtrack.notifications.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void shouldCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        NotificationDTO result = notificationService.createNotification(
                10L, "APPOINTMENT", "New appointment", "You have a new appointment", "/appointments/1");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("APPOINTMENT", result.getType());
        assertEquals("New appointment", result.getTitle());
        assertEquals("You have a new appointment", result.getBody());
        assertFalse(result.isRead());
        assertEquals("/appointments/1", result.getLink());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getUserId());
        assertFalse(captor.getValue().isRead());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    @Test
    void shouldReturnPaginatedNotifications() {
        Notification n1 = createNotification(1L, 10L, false);
        Notification n2 = createNotification(2L, 10L, true);
        Page<Notification> page = new PageImpl<>(List.of(n1, n2), PageRequest.of(0, 20), 2);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L, PageRequest.of(0, 20)))
                .thenReturn(page);

        NotificationPageDTO result = notificationService.getNotifications(10L, PageRequest.of(0, 20));

        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void shouldMarkSingleNotificationRead() {
        Notification notification = createNotification(1L, 10L, false);
        when(notificationRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO result = notificationService.markRead(1L, 10L);

        assertNotNull(result);
        assertTrue(result.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldReturnNullWhenMarkingNonExistentNotification() {
        when(notificationRepository.findByIdAndUserId(999L, 10L)).thenReturn(Optional.empty());

        assertNull(notificationService.markRead(999L, 10L));
    }

    @Test
    void shouldMarkAllNotificationsRead() {
        when(notificationRepository.markAllReadByUserId(10L)).thenReturn(3);

        notificationService.markAllRead(10L);

        verify(notificationRepository).markAllReadByUserId(10L);
    }

    @Test
    void shouldReturnUnreadCount() {
        when(notificationRepository.countByUserIdAndReadFalse(10L)).thenReturn(5L);

        assertEquals(5L, notificationService.getUnreadCount(10L));
    }

    @Test
    void shouldReturnZeroUnreadCountWhenNone() {
        when(notificationRepository.countByUserIdAndReadFalse(10L)).thenReturn(0L);

        assertEquals(0L, notificationService.getUnreadCount(10L));
    }

    private Notification createNotification(Long id, Long userId, boolean read) {
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(userId);
        n.setType("APPOINTMENT");
        n.setTitle("Test notification");
        n.setBody("Test body");
        n.setRead(read);
        n.setLink("/test/1");
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }
}

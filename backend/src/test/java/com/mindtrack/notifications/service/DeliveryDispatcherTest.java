package com.mindtrack.notifications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.notifications.dto.NotificationDTO;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryDispatcherTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserRepository userRepository;

    private DeliveryDispatcher deliveryDispatcher;

    @BeforeEach
    void setUp() {
        deliveryDispatcher = new DeliveryDispatcher(
                notificationService,
                emailService,
                userProfileRepository,
                userRepository,
                new ObjectMapper());
        when(notificationService.createNotification(
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(), anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new NotificationDTO());
    }

    @Test
    void shouldAlwaysCreateInAppNotification() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        deliveryDispatcher.dispatch(10L, "APPOINTMENT", "New appointment", "Body", "/link");

        verify(notificationService).createNotification(
                eq(10L), eq("APPOINTMENT"), eq("New appointment"), eq("Body"), eq("/link"));
    }

    @Test
    void shouldSendEmailWhenEmailNotificationsEnabled() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, "{\"emailNotifications\":true}")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "user@test.com")));

        deliveryDispatcher.dispatch(10L, "APPOINTMENT", "New appointment", "Body", "/link");

        verify(emailService).sendEmail(eq("user@test.com"), eq("New appointment"), contains("Body"));
    }

    @Test
    void shouldNotSendEmailWhenEmailNotificationsDisabled() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, "{\"emailNotifications\":false}")));

        deliveryDispatcher.dispatch(10L, "APPOINTMENT", "New appointment", "Body", null);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotSendEmailWhenNoPrefsExist() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, null)));

        deliveryDispatcher.dispatch(10L, "APPOINTMENT", "New appointment", "Body", null);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotSendEmailWhenNoProfileExists() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        deliveryDispatcher.dispatch(10L, "GOAL", "Goal achieved", null, "/goals/1");

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSkipEmailWhenUserHasNoEmail() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, "{\"emailNotifications\":true}")));
        User userWithoutEmail = createUser(10L, null);
        when(userRepository.findById(10L)).thenReturn(Optional.of(userWithoutEmail));

        deliveryDispatcher.dispatch(10L, "APPOINTMENT", "New appointment", "Body", null);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldUseBodyAsEmailBodyWhenPresent() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, "{\"emailNotifications\":true}")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "user@test.com")));

        deliveryDispatcher.dispatch(10L, "GOAL", "Goal reached", "You reached your step goal!", null);

        verify(emailService).sendEmail(
                eq("user@test.com"), eq("Goal reached"), eq("You reached your step goal!"));
    }

    @Test
    void shouldUseTitleAsEmailBodyWhenBodyIsNull() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(
                createProfile(10L, "{\"emailNotifications\":true}")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "user@test.com")));

        deliveryDispatcher.dispatch(10L, "REMINDER", "Don't forget your journal", null, null);

        verify(emailService).sendEmail(
                eq("user@test.com"), eq("Don't forget your journal"), eq("Don't forget your journal"));
    }

    private UserProfile createProfile(Long userId, String notificationPrefs) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNotificationPrefs(notificationPrefs);
        return profile;
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}

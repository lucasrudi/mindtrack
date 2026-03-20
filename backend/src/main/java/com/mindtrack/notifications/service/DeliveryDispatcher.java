package com.mindtrack.notifications.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Dispatches notifications to all applicable channels based on the user's notification preferences.
 *
 * <p>Always creates an in-app notification. Additionally routes to email when
 * {@code emailNotifications} is enabled in {@code user_profiles.notification_prefs}.
 */
@Service
public class DeliveryDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryDispatcher.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DeliveryDispatcher(NotificationService notificationService,
                              EmailService emailService,
                              UserProfileRepository userProfileRepository,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Dispatches a notification for the given user.
     *
     * <p>Always creates an in-app notification. Conditionally sends an email when the user
     * has {@code emailNotifications: true} in their notification preferences.
     *
     * @param userId user to notify
     * @param type   notification type (e.g. "APPOINTMENT", "GOAL_PROGRESS")
     * @param title  short notification title
     * @param body   longer notification body, may be null
     * @param link   deep-link path, may be null
     */
    public void dispatch(Long userId, String type, String title,
                         @Nullable String body, @Nullable String link) {
        notificationService.createNotification(userId, type, title, body, link);

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (isEmailEnabled(profile)) {
            sendEmailNotification(userId, title, body);
        }
    }

    private boolean isEmailEnabled(@Nullable UserProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.getNotificationPrefs())) {
            return false;
        }
        try {
            Map<String, Object> prefs = objectMapper.readValue(profile.getNotificationPrefs(), MAP_TYPE);
            return asBoolean(prefs.get("emailNotifications"));
        } catch (Exception ex) {
            LOG.warn("Failed to parse notification prefs for userId={}: {}",
                    profile.getUserId(), ex.getMessage());
            return false;
        }
    }

    private boolean asBoolean(@Nullable Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private void sendEmailNotification(Long userId, String title, @Nullable String body) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !StringUtils.hasText(userOpt.get().getEmail())) {
            LOG.warn("Skipping email notification for userId={} — no email found", userId);
            return;
        }
        String email = userOpt.get().getEmail();
        String emailBody = StringUtils.hasText(body) ? body : title;
        emailService.sendEmail(email, title, emailBody);
    }
}

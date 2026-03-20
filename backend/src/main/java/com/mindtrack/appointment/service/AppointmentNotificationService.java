package com.mindtrack.appointment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.common.model.User;
import com.mindtrack.messaging.service.TelegramService;
import com.mindtrack.messaging.service.WhatsAppService;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends patient-facing notifications after appointments are booked.
 */
@Service
public class AppointmentNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentNotificationService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' HH:mm");

    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    private final TelegramService telegramService;
    @Nullable
    private final WhatsAppService whatsAppService;
    @Nullable
    private final JavaMailSender mailSender;

    public AppointmentNotificationService(UserProfileRepository userProfileRepository,
                                          ObjectMapper objectMapper,
                                          TelegramService telegramService,
                                          Optional<WhatsAppService> whatsAppService,
                                          Optional<JavaMailSender> mailSender) {
        this.userProfileRepository = userProfileRepository;
        this.objectMapper = objectMapper;
        this.telegramService = telegramService;
        this.whatsAppService = whatsAppService.orElse(null);
        this.mailSender = mailSender.orElse(null);
    }

    /**
     * Notifies a patient after their therapist books an appointment.
     */
    public void notifyPatientAboutBooking(Appointment appointment, User therapist, User patient) {
        UserProfile profile = userProfileRepository.findByUserId(patient.getId()).orElse(null);
        NotificationPreferences preferences = resolvePreferences(profile);
        String message = buildMessage(appointment, therapist);

        if (preferences.pushEnabled()) {
            sendPushNotification(profile, patient.getId(), message);
        }
        if (preferences.emailEnabled()) {
            sendEmail(patient, therapist, message);
        }
    }

    private NotificationPreferences resolvePreferences(@Nullable UserProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.getNotificationPrefs())) {
            return new NotificationPreferences(true, false);
        }

        try {
            Map<String, Object> preferences = objectMapper.readValue(profile.getNotificationPrefs(), MAP_TYPE);
            return new NotificationPreferences(
                    asBoolean(preferences.get("emailNotifications")),
                    asBoolean(preferences.get("pushNotifications")));
        } catch (Exception ex) {
            LOG.warn("Failed to parse notification prefs for userId={}: {}", profile.getUserId(), ex.getMessage());
            return new NotificationPreferences(true, false);
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

    private void sendPushNotification(@Nullable UserProfile profile, Long patientId, String message) {
        if (profile == null) {
            LOG.info("Skipping appointment push notification for patientId={} because no profile exists", patientId);
            return;
        }

        if (StringUtils.hasText(profile.getTelegramChatId())) {
            telegramService.sendMessage(profile.getTelegramChatId(), message);
            LOG.info("Sent appointment Telegram notification to patientId={}", patientId);
            return;
        }

        if (StringUtils.hasText(profile.getWhatsappNumber())) {
            if (whatsAppService == null) {
                LOG.warn("Skipping appointment WhatsApp notification for patientId={} because WhatsApp is disabled",
                        patientId);
                return;
            }
            whatsAppService.sendMessage(profile.getWhatsappNumber(), message);
            LOG.info("Sent appointment WhatsApp notification to patientId={}", patientId);
            return;
        }

        LOG.info("Skipping appointment push notification for patientId={} because no linked channel exists",
                patientId);
    }

    private void sendEmail(User patient, User therapist, String message) {
        if (mailSender == null) {
            LOG.warn("Skipping appointment email notification for patientId={} because mail is not configured",
                    patient.getId());
            return;
        }
        if (!StringUtils.hasText(patient.getEmail())) {
            LOG.warn("Skipping appointment email notification for patientId={} because no email exists",
                    patient.getId());
            return;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(patient.getEmail());
        mailMessage.setSubject("New MindTrack appointment with " + displayName(therapist));
        mailMessage.setText(message);

        try {
            mailSender.send(mailMessage);
            LOG.info("Sent appointment email notification to patientId={}", patient.getId());
        } catch (MailException ex) {
            LOG.warn("Failed to send appointment email notification to patientId={}: {}",
                    patient.getId(), ex.getMessage());
        }
    }

    private String buildMessage(Appointment appointment, User therapist) {
        return "Your therapist " + displayName(therapist) + " booked a new appointment for "
                + appointment.getStartAt().format(DATE_TIME_FORMATTER) + ".\n"
                + "Ends at " + appointment.getEndAt().format(DateTimeFormatter.ofPattern("HH:mm")) + ".\n"
                + "Reason: " + defaultText(appointment.getReason(), "No reason provided") + ".";
    }

    private String displayName(User user) {
        if (StringUtils.hasText(user.getName())) {
            return user.getName().trim();
        }
        return user.getEmail();
    }

    private String defaultText(@Nullable String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private record NotificationPreferences(boolean emailEnabled, boolean pushEnabled) {
    }
}

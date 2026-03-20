package com.mindtrack.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.common.model.User;
import com.mindtrack.messaging.service.TelegramService;
import com.mindtrack.messaging.service.WhatsAppService;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private TelegramService telegramService;
    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private JavaMailSender mailSender;

    private AppointmentNotificationService appointmentNotificationService;

    @BeforeEach
    void setUp() {
        appointmentNotificationService = new AppointmentNotificationService(
                userProfileRepository,
                new ObjectMapper(),
                telegramService,
                Optional.of(whatsAppService),
                Optional.of(mailSender));
    }

    @Test
    void shouldSendEmailWhenEmailNotificationsEnabled() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(createProfile(
                10L, "{\"emailNotifications\":true,\"pushNotifications\":false}", null, null)));

        appointmentNotificationService.notifyPatientAboutBooking(
                createAppointment(),
                createUser(3L, "Dr. Lane", "therapist@test.com"),
                createUser(10L, "Patient One", "patient@test.com"));

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        assertEquals("patient@test.com", mailCaptor.getValue().getTo()[0]);
        assertEquals("New MindTrack appointment with Dr. Lane", mailCaptor.getValue().getSubject());
        assertTrue(mailCaptor.getValue().getText().contains("Weekly check-in"));
        verify(telegramService, never()).sendMessage(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldSendTelegramWhenPushNotificationsEnabled() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(createProfile(
                10L, "{\"emailNotifications\":false,\"pushNotifications\":true}", "123456", null)));

        appointmentNotificationService.notifyPatientAboutBooking(
                createAppointment(),
                createUser(3L, "Dr. Lane", "therapist@test.com"),
                createUser(10L, "Patient One", "patient@test.com"));

        verify(telegramService).sendMessage(org.mockito.ArgumentMatchers.eq("123456"),
                org.mockito.ArgumentMatchers.contains("Dr. Lane"));
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void shouldFallBackToWhatsAppWhenTelegramIsUnavailable() {
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(createProfile(
                10L, "{\"emailNotifications\":false,\"pushNotifications\":true}", null, "+1234567890")));

        appointmentNotificationService.notifyPatientAboutBooking(
                createAppointment(),
                createUser(3L, "Dr. Lane", "therapist@test.com"),
                createUser(10L, "Patient One", "patient@test.com"));

        verify(whatsAppService).sendMessage(org.mockito.ArgumentMatchers.eq("+1234567890"),
                org.mockito.ArgumentMatchers.contains("Weekly check-in"));
        verify(telegramService, never()).sendMessage(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldNotifyBothParticipantsWhenAppointmentIsCancelled() {
        when(userProfileRepository.findByUserId(3L)).thenReturn(Optional.of(createProfile(
                3L, null, "999", null)));
        when(userProfileRepository.findByUserId(10L)).thenReturn(Optional.of(createProfile(
                10L, null, null, null)));

        appointmentNotificationService.notifyParticipantsAboutCancellation(
                createAppointment(),
                createUser(3L, "Dr. Lane", "therapist@test.com"),
                createUser(10L, "Patient One", "patient@test.com"),
                createUser(10L, "Patient One", "patient@test.com"));

        verify(telegramService).sendMessage(org.mockito.ArgumentMatchers.eq("999"),
                org.mockito.ArgumentMatchers.contains("Appointment cancelled by Patient One"));
        verify(mailSender, times(2)).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    private Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setTherapistId(3L);
        appointment.setPatientId(10L);
        appointment.setStartAt(LocalDateTime.of(2026, 4, 20, 10, 0));
        appointment.setEndAt(LocalDateTime.of(2026, 4, 20, 10, 50));
        appointment.setReason("Weekly check-in");
        return appointment;
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private UserProfile createProfile(Long userId, String notificationPrefs,
                                      String telegramChatId, String whatsappNumber) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNotificationPrefs(notificationPrefs);
        profile.setTelegramChatId(telegramChatId);
        profile.setWhatsappNumber(whatsappNumber);
        return profile;
    }
}

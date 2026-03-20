package com.mindtrack.appointment.service;

import com.mindtrack.appointment.dto.AppointmentRequest;
import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.appointment.model.AppointmentStatus;
import com.mindtrack.appointment.repository.AppointmentRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TherapistPatientRepository therapistPatientRepository;
    @Mock
    private UserRepository userRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository, therapistPatientRepository, userRepository,
                new AppointmentMapper());
    }

    @Test
    void shouldListAppointmentsChronologically() {
        Appointment first = createAppointment(1L, 3L, 10L,
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 9, 50));
        Appointment second = createAppointment(2L, 3L, 11L,
                LocalDateTime.of(2025, 1, 11, 9, 0),
                LocalDateTime.of(2025, 1, 11, 9, 50));

        when(appointmentRepository.findByTherapistIdOrderByStartAtAsc(3L))
                .thenReturn(List.of(first, second));
        when(therapistPatientRepository.findByTherapistIdAndStatus(3L, TherapistPatientStatus.ACTIVE))
                .thenReturn(List.of(createRelationship(3L, 10L, "#f97316"),
                        createRelationship(3L, 11L, "#22c55e")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(userRepository.findById(11L)).thenReturn(Optional.of(createUser(11L, "Patient B")));

        List<AppointmentResponse> result = appointmentService.listAppointments(3L);

        assertEquals(2, result.size());
        assertEquals("Patient A", result.get(0).getPatientName());
        assertEquals("#f97316", result.get(0).getCalendarColor());
        assertEquals(50L, result.get(0).getDurationMinutes());
    }

    @Test
    void shouldBookAppointmentWhenNoConflictExists() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(appointmentRepository.findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of());
        when(appointmentRepository.findByPatientIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of());
        when(therapistPatientRepository.findByTherapistIdAndPatientId(3L, 10L))
                .thenReturn(Optional.of(createRelationship(3L, 10L, "#f97316")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment appointment = inv.getArgument(0);
            appointment.setId(99L);
            appointment.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            appointment.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            return appointment;
        });

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setEndAt(LocalDateTime.of(2025, 1, 20, 10, 50));
        request.setReason("Weekly check-in");

        AppointmentResponse result = appointmentService.bookAppointment(3L, 10L, request);

        assertEquals(99L, result.getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertEquals("Weekly check-in", result.getReason());
    }

    @Test
    void shouldRejectWhenTherapistAlreadyHasOverlappingAppointment() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(appointmentRepository.findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                any(), any(), any(), any()))
                .thenReturn(List.of(new Appointment()));

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setEndAt(LocalDateTime.of(2025, 1, 20, 10, 50));

        assertThrows(ResponseStatusException.class,
                () -> appointmentService.bookAppointment(3L, 10L, request));
    }

    @Test
    void shouldRejectWhenRelationshipIsNotActive() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(false);

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setEndAt(LocalDateTime.of(2025, 1, 20, 10, 50));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment(3L, 10L, request));
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setEndAt(LocalDateTime.of(2025, 1, 20, 10, 0));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment(3L, 10L, request));
    }

    private Appointment createAppointment(Long id, Long therapistId, Long patientId,
                                          LocalDateTime startAt, LocalDateTime endAt) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setTherapistId(therapistId);
        appointment.setPatientId(patientId);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason("Reason");
        appointment.setNotes("Notes");
        appointment.setCreatedAt(startAt);
        appointment.setUpdatedAt(endAt);
        return appointment;
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(' ', '.') + "@test.com");
        return user;
    }

    private TherapistPatient createRelationship(Long therapistId, Long patientId,
                                                String calendarColor) {
        TherapistPatient relationship = new TherapistPatient(therapistId, patientId,
                TherapistPatientStatus.ACTIVE);
        relationship.setCalendarColor(calendarColor);
        return relationship;
    }
}

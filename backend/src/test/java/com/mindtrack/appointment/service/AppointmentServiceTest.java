package com.mindtrack.appointment.service;

import com.mindtrack.appointment.dto.AppointmentRequest;
import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.appointment.model.AppointmentStatus;
import com.mindtrack.appointment.model.CancellationScope;
import com.mindtrack.appointment.model.RecurrenceType;
import com.mindtrack.appointment.repository.AppointmentRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TherapistPatientRepository therapistPatientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AppointmentNotificationService appointmentNotificationService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository, therapistPatientRepository, userRepository,
                new AppointmentMapper(), appointmentNotificationService);
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
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "Therapist A")));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment appointment = inv.getArgument(0);
            appointment.setId(99L);
            appointment.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            appointment.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            return appointment;
        });

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setDurationMinutes(50);
        request.setReason("Weekly check-in");

        AppointmentResponse result = appointmentService.bookAppointment(3L, 10L, request);

        assertEquals(99L, result.getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertEquals("Weekly check-in", result.getReason());
        assertEquals(50L, result.getDurationMinutes());
        verify(appointmentNotificationService).notifyPatientAboutBooking(any(), any(), any());
    }

    @Test
    void shouldComputeEndTimeFromDuration() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(appointmentRepository.findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of());
        when(appointmentRepository.findByPatientIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of());
        when(therapistPatientRepository.findByTherapistIdAndPatientId(3L, 10L))
                .thenReturn(Optional.of(createRelationship(3L, 10L, "#f97316")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "Therapist A")));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        when(appointmentRepository.save(captor.capture())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        LocalDateTime startAt = LocalDateTime.of(2025, 6, 1, 10, 0);
        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(startAt);
        request.setDurationMinutes(60);
        request.setReason("Intake");

        appointmentService.bookAppointment(3L, 10L, request);

        Appointment saved = captor.getValue();
        assertEquals(startAt.plusMinutes(60), saved.getEndAt());
    }

    @Test
    void shouldCreateWeeklyRecurringSeriesWithDefaultCount() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(therapistPatientRepository.findByTherapistIdAndPatientId(3L, 10L))
                .thenReturn(Optional.of(createRelationship(3L, 10L, "#f97316")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "Therapist A")));

        AtomicLong idGen = new AtomicLong(1);
        when(appointmentRepository.saveAll(any())).thenAnswer(inv -> {
            List<Appointment> list = inv.getArgument(0);
            list.forEach(a -> {
                a.setId(idGen.getAndIncrement());
                a.setCreatedAt(LocalDateTime.now());
                a.setUpdatedAt(LocalDateTime.now());
            });
            return list;
        });

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 7, 10, 0));
        request.setDurationMinutes(50);
        request.setReason("Weekly therapy");
        request.setRecurrence(RecurrenceType.WEEKLY);

        AppointmentResponse result = appointmentService.bookAppointment(3L, 10L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Appointment>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).saveAll(captor.capture());

        List<Appointment> saved = captor.getValue();
        assertEquals(12, saved.size());

        String seriesId = saved.get(0).getSeriesId();
        assertNotNull(seriesId);

        for (int i = 0; i < saved.size(); i++) {
            assertEquals(seriesId, saved.get(i).getSeriesId());
            assertEquals(i, saved.get(i).getSeriesIndex());
            assertEquals(LocalDateTime.of(2025, 1, 7, 10, 0).plusWeeks(i), saved.get(i).getStartAt());
        }

        assertNotNull(result);
        assertEquals("Weekly therapy", result.getReason());
        assertEquals(seriesId, result.getSeriesId());
    }

    @Test
    void shouldCreateWeeklySeriesWithCustomCount() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(therapistPatientRepository.findByTherapistIdAndPatientId(3L, 10L))
                .thenReturn(Optional.of(createRelationship(3L, 10L, "#f97316")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "Therapist A")));

        when(appointmentRepository.saveAll(any())).thenAnswer(inv -> {
            List<Appointment> list = inv.getArgument(0);
            AtomicLong idGen = new AtomicLong(1);
            list.forEach(a -> {
                a.setId(idGen.getAndIncrement());
                a.setCreatedAt(LocalDateTime.now());
                a.setUpdatedAt(LocalDateTime.now());
            });
            return list;
        });

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 3, 3, 14, 0));
        request.setDurationMinutes(45);
        request.setReason("Follow-up");
        request.setRecurrence(RecurrenceType.WEEKLY);
        request.setRecurrenceCount(6);

        appointmentService.bookAppointment(3L, 10L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Appointment>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).saveAll(captor.capture());
        assertEquals(6, captor.getValue().size());
    }

    @Test
    void shouldStopRecurringSeriesAtEndDate() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(therapistPatientRepository.findByTherapistIdAndPatientId(3L, 10L))
                .thenReturn(Optional.of(createRelationship(3L, 10L, "#f97316")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(createUser(10L, "Patient A")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "Therapist A")));

        when(appointmentRepository.saveAll(any())).thenAnswer(inv -> {
            List<Appointment> list = inv.getArgument(0);
            AtomicLong idGen = new AtomicLong(1);
            list.forEach(a -> {
                a.setId(idGen.getAndIncrement());
                a.setCreatedAt(LocalDateTime.now());
                a.setUpdatedAt(LocalDateTime.now());
            });
            return list;
        });

        // Start Jan 7, end Jan 28 — should produce exactly 4 occurrences
        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 7, 10, 0));
        request.setDurationMinutes(50);
        request.setReason("Therapy");
        request.setRecurrence(RecurrenceType.WEEKLY);
        request.setRecurrenceCount(20);
        request.setRecurrenceEndDate(LocalDate.of(2025, 1, 28));

        appointmentService.bookAppointment(3L, 10L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Appointment>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).saveAll(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    @Test
    void shouldCancelSingleAppointment() {
        Appointment appointment = createAppointment(5L, 3L, 10L,
                LocalDateTime.of(2025, 4, 1, 10, 0),
                LocalDateTime.of(2025, 4, 1, 10, 50));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        appointmentService.cancelAppointment(5L, 3L, CancellationScope.SINGLE);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
        verify(appointmentRepository, never()).updateStatusBySeriesId(anyString(), any());
        verify(appointmentRepository, never())
                .updateStatusBySeriesIdAndSeriesIndexGreaterThanEqual(anyString(), anyInt(), any());
    }

    @Test
    void shouldCancelThisAndFollowingInSeries() {
        Appointment appointment = createAppointment(5L, 3L, 10L,
                LocalDateTime.of(2025, 4, 1, 10, 0),
                LocalDateTime.of(2025, 4, 1, 10, 50));
        appointment.setSeriesId("series-uuid-123");
        appointment.setSeriesIndex(3);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        appointmentService.cancelAppointment(5L, 3L, CancellationScope.THIS_AND_FOLLOWING);

        verify(appointmentRepository).updateStatusBySeriesIdAndSeriesIndexGreaterThanEqual(
                eq("series-uuid-123"), eq(3), eq(AppointmentStatus.CANCELLED));
    }

    @Test
    void shouldCancelAllInSeries() {
        Appointment appointment = createAppointment(5L, 3L, 10L,
                LocalDateTime.of(2025, 4, 1, 10, 0),
                LocalDateTime.of(2025, 4, 1, 10, 50));
        appointment.setSeriesId("series-uuid-456");
        appointment.setSeriesIndex(0);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        appointmentService.cancelAppointment(5L, 3L, CancellationScope.ALL_IN_SERIES);

        verify(appointmentRepository).updateStatusBySeriesId(
                eq("series-uuid-456"), eq(AppointmentStatus.CANCELLED));
    }

    @Test
    void shouldRejectCancellationByDifferentTherapist() {
        Appointment appointment = createAppointment(5L, 3L, 10L,
                LocalDateTime.of(2025, 4, 1, 10, 0),
                LocalDateTime.of(2025, 4, 1, 10, 50));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(5L, 99L, CancellationScope.SINGLE));
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
        request.setDurationMinutes(50);
        request.setReason("Check-in");

        assertThrows(ResponseStatusException.class,
                () -> appointmentService.bookAppointment(3L, 10L, request));
    }

    @Test
    void shouldRejectWhenRelationshipIsNotActive() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 10L, TherapistPatientStatus.ACTIVE)).thenReturn(false);

        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2025, 1, 20, 10, 0));
        request.setDurationMinutes(50);
        request.setReason("Check-in");

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

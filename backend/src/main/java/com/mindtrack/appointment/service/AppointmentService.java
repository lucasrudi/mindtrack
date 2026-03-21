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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for therapist appointment booking and calendar access.
 */
@Service
public class AppointmentService {

    private static final String PATIENT_NOT_FOUND_PREFIX = "Patient not found: ";
    private static final int DEFAULT_RECURRENCE_COUNT = 12;

    private final AppointmentRepository appointmentRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentNotificationService appointmentNotificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TherapistPatientRepository therapistPatientRepository,
                              UserRepository userRepository,
                              AppointmentMapper appointmentMapper,
                              AppointmentNotificationService appointmentNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.appointmentMapper = appointmentMapper;
        this.appointmentNotificationService = appointmentNotificationService;
    }

    /**
     * Lists all appointments for the therapist, ordered chronologically.
     */
    public List<AppointmentResponse> listAppointments(Long therapistId) {
        List<Appointment> appointments = appointmentRepository
                .findByTherapistIdOrderByStartAtAsc(therapistId);
        Map<Long, String> calendarColors = new HashMap<>();
        therapistPatientRepository.findByTherapistIdAndStatus(therapistId,
                TherapistPatientStatus.ACTIVE).forEach(rel ->
                calendarColors.put(rel.getPatientId(), rel.getCalendarColor()));
        return appointments.stream()
                .map(appointment -> appointmentMapper.toResponse(
                        appointment,
                        loadPatient(appointment.getPatientId()),
                        calendarColors.get(appointment.getPatientId())))
                .toList();
    }

    /**
     * Books a new appointment or creates a recurring series based on the recurrence field.
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long therapistId, Long patientId,
                                               AppointmentRequest request) {
        validateRelationship(therapistId, patientId);

        if (request.getRecurrence() == RecurrenceType.WEEKLY) {
            return createRecurringSeries(therapistId, patientId, request);
        }
        return createSingleAppointment(therapistId, patientId, request);
    }

    /**
     * Creates a single non-recurring appointment.
     */
    private AppointmentResponse createSingleAppointment(Long therapistId, Long patientId,
                                                         AppointmentRequest request) {
        LocalDateTime startAt = request.getStartAt();
        LocalDateTime endAt = startAt.plusMinutes(request.getDurationMinutes());
        validateConflicts(therapistId, patientId, startAt, endAt);

        Appointment appointment = buildAppointment(therapistId, patientId, startAt, endAt,
                request.getDurationMinutes(), request.getReason(), request.getNotes(),
                null, null, null);

        User patient = loadUser(patientId, PATIENT_NOT_FOUND_PREFIX);
        Appointment saved = appointmentRepository.save(appointment);
        appointmentNotificationService.notifyPatientAboutBooking(
                saved,
                loadUser(therapistId, "Therapist not found: "),
                patient);
        return appointmentMapper.toResponse(saved, patient, loadCalendarColor(therapistId, patientId));
    }

    /**
     * Creates a weekly recurring series of appointments sharing a series ID.
     */
    private AppointmentResponse createRecurringSeries(Long therapistId, Long patientId,
                                                       AppointmentRequest request) {
        String seriesId = UUID.randomUUID().toString();
        int count = resolveRecurrenceCount(request);
        LocalDate endDate = request.getRecurrenceEndDate();

        List<Appointment> occurrences = new ArrayList<>();
        LocalDateTime baseStart = request.getStartAt();

        for (int i = 0; i < count; i++) {
            LocalDateTime startAt = baseStart.plusWeeks(i);
            if (endDate != null && startAt.toLocalDate().isAfter(endDate)) {
                break;
            }
            LocalDateTime endAt = startAt.plusMinutes(request.getDurationMinutes());
            occurrences.add(buildAppointment(therapistId, patientId, startAt, endAt,
                    request.getDurationMinutes(), request.getReason(), request.getNotes(),
                    RecurrenceType.WEEKLY.name(), seriesId, i));
        }

        if (occurrences.isEmpty()) {
            throw new IllegalArgumentException("No occurrences generated for the given recurrence parameters");
        }

        List<Appointment> saved = appointmentRepository.saveAll(occurrences);
        User patient = loadUser(patientId, PATIENT_NOT_FOUND_PREFIX);
        User therapist = loadUser(therapistId, "Therapist not found: ");
        appointmentNotificationService.notifyPatientAboutBooking(saved.get(0), therapist, patient);
        String calendarColor = loadCalendarColor(therapistId, patientId);
        return appointmentMapper.toResponse(saved.get(0), patient, calendarColor);
    }

    /**
     * Cancels one or more appointments based on the given scope.
     * SINGLE: cancels only the specified appointment.
     * THIS_AND_FOLLOWING: cancels this occurrence and all later ones in the series.
     * ALL_IN_SERIES: cancels every occurrence in the series.
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, Long therapistId, CancellationScope scope) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment not found: " + appointmentId));

        if (!appointment.getTherapistId().equals(therapistId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied to appointment: " + appointmentId);
        }

        switch (scope) {
            case SINGLE -> cancelSingle(appointment);
            case THIS_AND_FOLLOWING -> cancelThisAndFollowing(appointment);
            case ALL_IN_SERIES -> cancelAllInSeries(appointment);
            default -> throw new IllegalArgumentException("Unknown cancellation scope: " + scope);
        }
    }

    private void cancelSingle(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private void cancelThisAndFollowing(Appointment appointment) {
        if (appointment.getSeriesId() == null || appointment.getSeriesIndex() == null) {
            cancelSingle(appointment);
            return;
        }
        appointmentRepository.updateStatusBySeriesIdAndSeriesIndexGreaterThanEqual(
                appointment.getSeriesId(), appointment.getSeriesIndex(), AppointmentStatus.CANCELLED);
    }

    private void cancelAllInSeries(Appointment appointment) {
        if (appointment.getSeriesId() == null) {
            cancelSingle(appointment);
            return;
        }
        appointmentRepository.updateStatusBySeriesId(appointment.getSeriesId(),
                AppointmentStatus.CANCELLED);
    }

    private int resolveRecurrenceCount(AppointmentRequest request) {
        if (request.getRecurrenceCount() != null && request.getRecurrenceCount() > 0) {
            return request.getRecurrenceCount();
        }
        return DEFAULT_RECURRENCE_COUNT;
    }

    private Appointment buildAppointment(Long therapistId, Long patientId,
                                          LocalDateTime startAt, LocalDateTime endAt,
                                          int durationMinutes, String reason, String notes,
                                          String recurrenceRule, String seriesId,
                                          Integer seriesIndex) {
        Appointment appointment = new Appointment();
        appointment.setTherapistId(therapistId);
        appointment.setPatientId(patientId);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setDurationMinutes(durationMinutes);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(reason);
        appointment.setNotes(notes);
        appointment.setRecurrenceRule(recurrenceRule);
        appointment.setSeriesId(seriesId);
        appointment.setSeriesIndex(seriesIndex);
        return appointment;
    }

    private void validateRelationship(Long therapistId, Long patientId) {
        boolean active = therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                therapistId, patientId, TherapistPatientStatus.ACTIVE);
        if (!active) {
            throw new IllegalArgumentException("Active therapist-patient relationship not found");
        }
    }

    private void validateConflicts(Long therapistId, Long patientId,
                                   LocalDateTime startAt, LocalDateTime endAt) {
        List<AppointmentStatus> blockingStatuses = List.of(AppointmentStatus.SCHEDULED);

        boolean therapistConflict = !appointmentRepository
                .findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        therapistId, blockingStatuses, endAt, startAt)
                .isEmpty();
        if (therapistConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Therapist already has an appointment during this time");
        }

        boolean patientConflict = !appointmentRepository
                .findByPatientIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        patientId, blockingStatuses, endAt, startAt)
                .isEmpty();
        if (patientConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Patient already has an appointment during this time");
        }
    }

    private User loadPatient(Long patientId) {
        return loadUser(patientId, PATIENT_NOT_FOUND_PREFIX);
    }

    private User loadUser(Long userId, String notFoundPrefix) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(notFoundPrefix + userId));
    }

    private String loadCalendarColor(Long therapistId, Long patientId) {
        return therapistPatientRepository.findByTherapistIdAndPatientId(therapistId, patientId)
                .filter(rel -> rel.getStatus() == TherapistPatientStatus.ACTIVE)
                .map(TherapistPatient::getCalendarColor)
                .orElse(null);
    }
}
